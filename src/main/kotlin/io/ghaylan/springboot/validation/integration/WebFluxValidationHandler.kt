package io.ghaylan.springboot.validation.integration

import io.ghaylan.springboot.validation.engine.ValidatorEngine
import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.ext.getUniqueIdentifier
import io.ghaylan.springboot.validation.ext.pathVariableName
import io.ghaylan.springboot.validation.ext.requestHeaderName
import io.ghaylan.springboot.validation.ext.requestParamName
import io.ghaylan.springboot.validation.model.errors.ApiError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.jvm.kotlinFunction

/**
 * Executes validation for Spring WebFlux controllers by routing to strategy-specific
 * handlers based on method return type (Mono, Flux, Flow, suspend, or synchronous).
 *
 * Resolves reactive parameters non-blockingly and delegates to [ValidatorEngine].
 */
object WebFluxValidationHandler
{

    /** Routes to Mono, Flux/Flow, or sync validation based on the method's return type. */
    fun validate(
        method: Method,
        parameters: Array<Parameter>,
        args: Array<Any?>,
        validatorEngine: ValidatorEngine,
        joinPoint: ProceedingJoinPoint
    ) : Any?
    {
        val requestId = method.getUniqueIdentifier()

        val classifier = method.kotlinFunction?.returnType?.classifier

        val isSuspend = method.kotlinFunction?.isSuspend == true
        val isFlow = classifier == Flow::class
        val isMono = classifier == Mono::class
        val isFlux = classifier == Flux::class

        return when {
            isFlow || isFlux -> handleFluxReturn(requestId, parameters, args, validatorEngine, joinPoint)
            isMono || isSuspend -> handleMonoReturn(requestId, parameters, args, validatorEngine, joinPoint)
            else -> handleSyncReturn(requestId, parameters, args, validatorEngine, joinPoint)
        }
    }


    /**
     * Handles validation for methods returning Mono.
     * Validates first, then proceeds with the original method.
     */
    private fun handleMonoReturn(
        requestId: String,
        parameters: Array<Parameter>,
        args: Array<Any?>,
        validatorEngine: ValidatorEngine,
        joinPoint: ProceedingJoinPoint
    ) : Mono<*> {
        return validateReactively(requestId, parameters, args, validatorEngine)
            .then(proceedWithMono(joinPoint, args))
    }


    /**
     * Handles validation for methods returning Flux.
     * Validates first, then proceeds with the original method.
     */
    private fun handleFluxReturn(
        requestId: String,
        parameters: Array<Parameter>,
        args: Array<Any?>,
        validatorEngine: ValidatorEngine,
        joinPoint: ProceedingJoinPoint
    ) : Flux<*>
    {
        return validateReactively(requestId, parameters, args, validatorEngine)
            .thenMany(proceedWithFlux(joinPoint, args))
    }


    /**
     * Handles validation for synchronous methods.
     */
    private fun handleSyncReturn(
        requestId: String,
        parameters: Array<Parameter>,
        args: Array<Any?>,
        validatorEngine: ValidatorEngine,
        joinPoint: ProceedingJoinPoint
    ) : Any?
    {
        val errors = mono {
            runValidation(
                requestId = requestId,
                parameters = parameters,
                resolvedArgs = args,
                validatorEngine = validatorEngine)
        }.block()

        if (!errors.isNullOrEmpty()) {
            throw ConstraintViolationException(errors = errors)
        }

        return joinPoint.proceed(args)
    }


    private fun proceedWithMono(
        joinPoint: ProceedingJoinPoint,
        args: Array<Any?>
    ) : Mono<*>
    {
        return runCatching {
            joinPoint.proceed(args) as Mono<*>
        }.getOrElse { Mono.error<Any>(it) }
    }


    private fun proceedWithFlux(
        joinPoint: ProceedingJoinPoint,
        args: Array<Any?>
    ) : Flux<*>
    {
        return runCatching {
            joinPoint.proceed(args) as Flux<*>
        }.getOrElse { Flux.error<Any>(it) }
    }


    /**
     * Resolves reactive parameters (`Mono<T>`, `Flux<T>`, `Flow<T>`) without blocking.
     */
    private fun resolveReactiveParameters(
        parameters: Array<Parameter>,
        args: Array<Any?>
    ) : Mono<Array<out Any?>>
    {
        val resolvedMonos = parameters.mapIndexed { i, _ ->
            val arg = args[i]
            resolveArgReactively(arg)
        }

        // Combine all Monos into a single array
        return Mono.zip(resolvedMonos) { it }
    }


    /**
     * Resolves a single argument reactively.
     */
    private fun resolveArgReactively(arg: Any?): Mono<Any>
    {
        return when (arg)
        {
            null -> Mono.empty()
            is Mono<*> -> {
                arg.cast(Any::class.java)
                    .onErrorResume(ClassCastException::class.java) { Mono.empty() }
                    .map { it }
            }
            is Flux<*> -> {
                arg.collectList().map { it }
            }
            is Flow<*> -> {
                @Suppress("UNCHECKED_CAST")
                val flow = arg as Flow<Any>
                Flux.from(flow.asPublisher())
                    .collectList()
                    .map { it }
            }
            else -> Mono.just(arg)
        }
    }
	

    /** Resolves reactive parameters, runs validation, and emits errors through the Mono chain. */
    private fun validateReactively(
        requestId: String,
        parameters: Array<Parameter>,
        args: Array<Any?>,
        validatorEngine: ValidatorEngine
    ) : Mono<Unit>
    {
        return resolveReactiveParameters(parameters, args)
	        .flatMap { resolvedArgs ->
	            // Run validation on worker thread to avoid blocking event loop
	            mono {
	                runValidation(requestId, parameters, resolvedArgs, validatorEngine)
	            }
	        }
	        .flatMap { errors ->
	            if (errors.isNotEmpty()) {
	                Mono.error(ConstraintViolationException(errors = errors))
	            } else {
	                Mono.just(Unit)
	            }
	        }
    }

    /**
     * Runs validation logic by extracting HTTP request details from parameters and invoking the validator.
     *
     * @param parameters Java reflection array of method parameters.
     * @param resolvedArgs Array of resolved argument values matching [parameters].
     * @param validatorEngine The validation engine responsible for checking constraints.
     * @return A list of validation [ApiError]s, empty if no violations found.
     */
    private suspend fun runValidation(
        requestId : String,
        parameters: Array<Parameter>,
        resolvedArgs: Array<out Any?>,
        validatorEngine: ValidatorEngine,
    ) : List<ApiError> {
        val zipped = parameters.zip(resolvedArgs.asList())

        var requestBody: Any? = null
        val queryParams = mutableMapOf<String, Any?>()
        val headers = mutableMapOf<String, Any?>()
        val pathVariables = mutableMapOf<String, Any?>()

        for ((param, value) in zipped) {
            when {
                param.isAnnotationPresent(RequestBody::class.java) && requestBody == null -> requestBody = value
                param.isAnnotationPresent(RequestParam::class.java) -> queryParams[param.requestParamName()] = value
                param.isAnnotationPresent(RequestHeader::class.java) -> headers[param.requestHeaderName()] = value
                param.isAnnotationPresent(PathVariable::class.java) -> pathVariables[param.pathVariableName()] = value
            }
        }

        return validatorEngine.validateRequest(
            id = requestId,
            body = requestBody,
            params = queryParams.ifEmpty { null },
            headers = headers.ifEmpty { null },
            pathVariables = pathVariables.ifEmpty { null })
    }
}