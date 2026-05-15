package io.ghaylan.springboot.validation.integration

import io.ghaylan.springboot.validation.engine.ValidatorEngine
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature

/**
 * AOP aspect that intercepts `@ValidateInput` methods on `@RestController` classes
 * and delegates to [WebFluxValidationHandler] for automatic request validation.
 *
 * Supports Mono, Flux, Flow, suspend functions, and synchronous return types.
 */
@Aspect
open class ValidationAspect(private val validatorEngine: ValidatorEngine) {
	
    /** Intercepts `@ValidateInput` methods. Skips parameterless methods for zero overhead. */
    @Around("@annotation(io.ghaylan.springboot.validation.integration.ValidateInput) && within(@org.springframework.web.bind.annotation.RestController *)")
    open fun run(joinPoint: ProceedingJoinPoint): Any? {
        val method = (joinPoint.signature as? MethodSignature?)?.method ?: return joinPoint.proceed()

        // Early exit for methods without parameters
        if (method.parameterCount == 0) {
            return joinPoint.proceed()
        }

        return WebFluxValidationHandler.validate(
            method = method,
            parameters = method.parameters,
            args = joinPoint.args,
            validatorEngine = validatorEngine,
            joinPoint = joinPoint)
    }
}