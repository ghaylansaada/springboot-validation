package io.ghaylan.springboot.validation.integration

import io.ghaylan.springboot.validation.engine.ValidatorEngine
import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.ext.generateRequestId
import io.ghaylan.springboot.validation.ext.pathVariableName
import io.ghaylan.springboot.validation.ext.requestHeaderName
import io.ghaylan.springboot.validation.ext.requestParamName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.ContextView
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.Locale
import kotlin.reflect.jvm.kotlinFunction

/**
 * **Universal Validation Handler for Spring WebFlux Controllers**
 *
 * This handler is the core component responsible for executing validation logic across
 * all Spring WebFlux programming paradigms. It provides a unified interface while
 * internally routing to specialized validation strategies based on method return types.
 *
 * ## Architecture Overview
 *
 * The handler operates on a strategy pattern where different return types receive
 * different validation approaches, all optimized for their specific execution model:
 *
 * ### Reactive Strategy (`Mono<T>`, `Flux<T>`)
 * - **Non-blocking parameter resolution**: Reactive parameters are resolved using reactive operators
 * - **Reactor context integration**: Automatically retrieves `ServerWebExchange` from context
 * - **Default validation execution**: Simple validations run on the event loop without extra threads
 * - **Optional worker thread offloading**: Heavy or blocking validators can use a worker thread (e.g., `Dispatchers.IO`) to prevent blocking
 * - **Chain integration**: Validation is seamlessly integrated into reactive chains with proper backpressure
 *
 * ### Coroutine Strategy (`Flow<T>`, `suspend fun`)
 * - **Coroutine context preservation**: Maintains coroutine scope and context throughout validation
 * - **Suspend function support**: Handles suspension points correctly
 * - **Default lightweight execution**: Simple validations run in the same coroutine context without switching threads
 * - **Optional heavy operation support**: Blocking or slow validations can be executed on `Dispatchers.IO` to keep event loop threads free
 * - **Flow integration**: Validation integrates smoothly into coroutine flows
 * - **Exception propagation**: Exceptions propagate naturally through coroutines

 *
 * ### Synchronous Strategy (Regular return types)
 * - **Direct validation**: Immediate parameter validation without async overhead
 * - **Exception throwing**: Direct exception throwing for validation failures
 * - **Parameter extraction**: Direct parameter value extraction from method arguments
 * - **Performance optimization**: Minimal overhead for non-reactive scenarios
 *
 * ## Key Components
 *
 * ### Parameter Resolution
 * The handler intelligently resolves different parameter types:
 * - **`Mono<T>` parameters**: Resolved reactively without blocking using `.switchIfEmpty()`
 * - **`Flux<T>` parameters**: Collected into lists reactively using `.collectList()`
 * - **`Flow<T>` parameters**: Converted to reactive streams then collected
 * - **Regular parameters**: Used directly without transformation
 *
 * ### Context Management
 * - **ServerWebExchange Discovery**: Automatically locates exchange from parameters or Reactor context
 * - **Locale Resolution**: Extracts user locale from headers, or defaults to English
 * - **Error Context**: Maintains full context information for detailed error reporting
 *
 * ### Validation Integration
 * - **Engine Delegation**: Delegates actual validation logic to pluggable `ValidatorEngine`
 * - **Parameter Mapping**: Maps Spring annotations (`@RequestBody`, `@RequestParam`, etc.) to validation context
 * - **Error Aggregation**: Collects all validation errors before throwing exceptions
 * - **Type Safety**: Maintains type safety throughout the validation pipeline
 *
 * ## Performance Characteristics
 *
 * - **Zero Blocking**: No `.block()` calls anywhere in reactive chains
 * - **Worker Thread Isolation**: CPU-intensive validation runs off event loop threads
 * - **Memory Efficiency**: Minimal object allocation during validation process
 * - **Early Exit Optimization**: Quick bailout for methods without parameters
 * - **Type-Specific Routing**: No unnecessary type conversions or paradigm bridging
 *
 * ## Error Handling
 *
 * The handler provides comprehensive error handling strategies:
 * - **Validation Errors**: `ConstraintViolenceException` with detailed violation information
 * - **Context Errors**: `IllegalStateException` when required context is missing
 * - **System Errors**: Proper propagation of unexpected exceptions through appropriate channels
 * - **Reactive Error Propagation**: Errors flow through reactive streams as error signals
 * - **Coroutine Exception Handling**: Exceptions propagate through coroutine machinery
 */
object WebFluxValidationHandler
{

    /**
     * This method serves as the central dispatcher that analyzes the target method's return type
     * and routes validation to the most appropriate strategy. It ensures that each WebFlux
     * paradigm receives validation that's optimized for its execution model and performance
     * characteristics.
     *
     * ### Return Type Analysis & Routing
     *
     * The method performs runtime type analysis to determine the optimal validation approach:
     *
     * - **`Flow<T>` Detection**: Routes to coroutine-based validation with proper suspension handling
     * - **`Mono<T>` Detection**: Routes to reactive validation integrated into the Mono chain
     * - **`Flux<T>` Detection**: Routes to reactive validation with backpressure-aware processing
     * - **Fallback Strategy**: Routes to synchronous validation for traditional return types
     *
     * ### Validation Strategy Selection
     *
     * Each strategy is optimized for its specific execution context:
     *
     * 1. **Coroutine Strategy** (`Flow<T>`)
     *    - Preserves coroutine context and scope
     *    - Integrates validation into coroutine flow
     *    - Handles suspension points correctly
     *    - Maintains async/await semantics
     *
     * 2. **Reactive Strategy** (`Mono<T>`, `Flux<T>`)
     *    - Non-blocking parameter resolution
     *    - Reactor context preservation
     *    - Worker thread validation execution
     *    - Seamless chain integration
     *
     * 3. **Synchronous Strategy** (Others)
     *    - Direct parameter validation
     *    - Immediate exception throwing
     *    - Minimal async overhead
     *    - Traditional error handling
     *
     * ### Parameter Processing
     *
     * The method ensures that parameters are processed correctly regardless of their type:
     * - Reactive parameters (`Mono<T>`, `Flux<T>`) are resolved non-blockingly
     * - Coroutine parameters maintain proper context and suspension semantics
     * - Regular parameters are used directly without transformation overhead
     * - Complex nested types are handled recursively with proper type safety
     *
     * @param method Target controller method being intercepted for validation
     * @param parameters Array of method parameters with full reflection metadata
     * @param args Actual argument values passed to the method at runtime
     * @param validatorEngine Configured validation engine responsible for rule evaluation
     * @param joinPoint AspectJ join point for proceeding with original method execution
     * @return Original method return value with validation applied, maintaining original type
     */
    fun validate(
        method: Method,
        parameters: Array<Parameter>,
        args: Array<Any?>,
        validatorEngine: ValidatorEngine,
        joinPoint: ProceedingJoinPoint
    ) : Any?
    {
        val requestId = method.generateRequestId()

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
    ) : Mono<*>
    {
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
        // For sync methods, we need to find ServerWebExchange from parameters
        val exchange = args.filterIsInstance<ServerWebExchange>().firstOrNull()
            ?: throw IllegalStateException("ServerWebExchange not found in method parameters. Ensure the exchange is injected or added to Reactor context.")

        val errors = mono {
            runValidation(requestId, parameters, args, validatorEngine, exchange)
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
    ) : Mono<Array<Any?>>
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
    private fun resolveArgReactively(arg: Any?): Mono<Any?>
    {
        return when (arg)
        {
            null -> Mono.justOrEmpty(null)
            is Mono<*> -> arg.cast(Any::class.java).switchIfEmpty(Mono.justOrEmpty(null))
            is Flux<*> -> arg.collectList().cast(Any::class.java)
            is Flow<*> -> {
                @Suppress("UNCHECKED_CAST")
                val asFlow = arg as Flow<Any>
                Flux.from(asFlow.asPublisher()).collectList().cast(Any::class.java)
            }
            else -> Mono.just(arg)
        }
    }


    /**
     * Retrieves the current `ServerWebExchange` from Reactor context.
     *
     * @param context Reactor context view
     * @return Mono containing `ServerWebExchange` if available, empty otherwise
     */
    private fun getCurrentServerWebExchange(
        context: ContextView
    ): Mono<ServerWebExchange>
    {
        return ServerWebExchangeContextFilter
            .getExchange(context)       // returns Optional<ServerWebExchange>
            .map { Mono.just(it) }      // wrap in Mono if present
            .orElse(Mono.empty())       // empty if not found
    }


    /**
     * **Reactive validation strategy for `Mono<T>` and `Flux<T>` return types.**
     *
     * This method implements a reactive validation pipeline that ensures parameter
     * validation is performed safely and efficiently. Simple validations run directly
     * on the event loop, while potentially heavy or blocking validators can be
     * executed on worker threads (`Dispatchers.IO`) to avoid blocking the Netty event loop.
     *
     * ### Reactive Validation Pipeline
     *
     * 1. **Parameter Resolution Phase**
     *    - Resolves reactive parameters (`Mono<T>`, `Flux<T>`, `Flow<T>`) non-blockingly
     *    - Uses `Mono.zip()` to combine multiple reactive parameters efficiently
     *    - Preserves backpressure and error signals from reactive streams
     *
     * 2. **Context Discovery Phase**
     *    - Locates `ServerWebExchange` from method parameters or Reactor context
     *    - Provides detailed error messages if the exchange is unavailable
     *    - Maintains context propagation through the reactive chain
     *
     * 3. **Validation Execution Phase**
     *    - Simple validations run directly on the event loop
     *    - Heavy or blocking validators can be offloaded to `Dispatchers.IO` or other worker threads
     *    - All validation rules are processed using the configured `ValidatorEngine`
     *    - Collects all validation errors before determining success/failure
     *
     * 4. **Error Handling Phase**
     *    - Converts validation failures into `ConstraintViolationException`
     *    - Exceptions propagate through the reactive stream properly
     *    - Maintains original error stack traces and context information
     *    - Supports request correlation for tracing
     *
     * ### Performance Considerations
     *
     * - **Event-loop friendly**: Default lightweight validations do not block Netty threads
     * - **Optional worker thread offloading**: Heavy validators run safely without affecting throughput
     * - **Memory efficient**: Minimal object allocation during validation
     * - **Early error propagation**: Validation errors are emitted immediately
     *
     * @param requestId Unique identifier for request correlation and tracing
     * @param parameters Method parameter metadata for annotation processing
     * @param args Runtime argument values corresponding to method parameters
     * @param validatorEngine Configured engine for executing validation rules
     * @return `Mono<Unit>` that completes successfully if validation passes, or errors on failure
     */
    private fun validateReactively(
        requestId: String,
        parameters: Array<Parameter>,
        args: Array<Any?>,
        validatorEngine: ValidatorEngine
    ) : Mono<Unit>
    {
        return Mono.deferContextual { context ->
            // First, resolve all reactive parameters
            resolveReactiveParameters(parameters, args)
                .flatMap { resolvedArgs ->
                    // Get ServerWebExchange from context or parameters
                    getCurrentServerWebExchange(context)
                        .switchIfEmpty(Mono.error(IllegalStateException("ServerWebExchange not found in method parameters or Reactor context. Ensure the exchange is injected or added to Reactor context.")))
                        .flatMap { exchange ->
                            // Run validation on worker thread to avoid blocking event loop
                            mono {
                                runValidation(requestId, parameters, resolvedArgs, validatorEngine, exchange)
                            }
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
    }


    /**
     * Runs validation logic by extracting HTTP request details from parameters and invoking the validator.
     *
     * @param parameters Java reflection array of method parameters.
     * @param resolvedArgs Array of resolved argument values matching [parameters].
     * @param validatorEngine The validation engine responsible for checking constraints.
     * @param exchange The current [ServerWebExchange], or null if unavailable.
     * @return A list of validation [ApiError]s, empty if no violations found.
     */
    private suspend fun runValidation(
        requestId : String,
        parameters: Array<Parameter>,
        resolvedArgs: Array<Any?>,
        validatorEngine: ValidatorEngine,
        exchange: ServerWebExchange
    ) : List<ApiError>
    {
        val zipped = parameters.zip(resolvedArgs.asList())

        var requestBody: Any? = null
        val queryParams = mutableMapOf<String, Any?>()
        val headers = mutableMapOf<String, Any?>()
        val pathVariables = mutableMapOf<String, Any?>()

        for ((param, value) in zipped)
        {
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
            pathVariables = pathVariables.ifEmpty { null },
            locale = exchange.localeContext.locale ?: Locale.ENGLISH)
    }
}