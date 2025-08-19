package io.ghaylan.springboot.validation.integration

import io.ghaylan.springboot.validation.engine.ValidatorEngine
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException

/**
 * **Universal Validation Aspect for Spring WebFlux Controllers**
 *
 * This aspect provides automatic, annotation-driven validation for all Spring WebFlux paradigms,
 * eliminating the need for manual validation calls in controller methods. It seamlessly integrates
 * with reactive streams, coroutines, and synchronous methods without blocking operations.
 *
 * ## Supported WebFlux Patterns
 *
 * ### Reactive Programming
 * - **`Mono<T>`**: Single-value reactive streams with non-blocking validation
 * - **`Flux<T>`**: Multi-value reactive streams with backpressure-aware validation
 *
 * ### Kotlin Coroutines
 * - **`suspend fun`**: Coroutine-based functions with context-preserving validation
 * - **`Flow<T>`**: Reactive streams using Kotlin coroutines with async validation
 *
 * ### Traditional Synchronous
 * - Not supported for now
 *
 * ## How It Works
 *
 * The aspect intercepts methods annotated with `@ValidateInput` and automatically validates
 * request parameters before the controller method executes. Validation is integrated into
 * the reactive chain for reactive types, runs in coroutine context for suspend functions,
 * and executes synchronously for regular methods.
 *
 * ## Key Features
 *
 * - **Zero Intrusion**: Controllers don't need explicit validation calls
 * - **Non-Blocking**: All validation preserves reactive/async characteristics
 * - **Automatic Routing**: Detects return type and applies appropriate validation strategy
 * - **Context Preservation**: Maintains Reactor context and coroutine scope
 * - **Performance Optimized**: Validation runs on worker threads to avoid blocking event loops
 * - **Exception Integration**: Validation failures propagate naturally through reactive/async chains
 *
 * ## Requirements
 *
 * 1. **Method Annotation**: `@ValidateInput` on controller methods
 * 2. **Class Annotation**: `@RestController` on controller classes
 * 3. **ServerWebExchange**: Available in method parameters or Reactor context
 * 4. **Validation Engine**: Configured `ValidatorEngine` bean
 * 5. **AOP Configuration**: AspectJ or Spring AOP properly configured
 */
@Aspect
open class ValidationAspect(private val validatorEngine: ValidatorEngine)
{

    /**
     * This method is triggered by AspectJ when a controller method annotated with `@ValidateInput`
     * is called. It performs several key operations:
     *
     * ### Execution Flow
     * 1. **Method Extraction**: Extracts target method information from the join point
     * 2. **Parameter Check**: Early exit optimization for parameterless methods
     * 3. **Type Detection**: Determines return type to choose validation strategy
     * 4. **Validation Routing**: Delegates to appropriate handler based on detected type
     * 5. **Chain Integration**: Seamlessly integrates validation into execution flow
     *
     * ### Validation Strategies by Return Type
     * - **`Flow<T>`**: Uses coroutine-based validation with `suspend` context preservation
     * - **`Mono<T>`**: Reactive validation integrated into the `Mono` chain without blocking
     * - **`Flux<T>`**: Reactive validation integrated into the `Flux` stream with backpressure
     * - **Others**: Synchronous validation not yet supported
     *
     * ### Error Handling
     * - **Validation Failures**: `ConstraintViolenceException` thrown/propagated appropriately
     * - **Missing Parameters**: Method proceeds without validation (early optimization)
     * - **Context Errors**: Proper error propagation through reactive/async chains
     *
     * ### Performance Characteristics
     * - **Early Exit**: Zero overhead for methods without parameters
     * - **Type-Specific**: No unnecessary conversions between paradigms
     * - **Worker Threads**: Validation runs on `boundedElastic()` scheduler for reactive types
     * - **Memory Efficient**: Minimal object allocation during validation process
     *
     * @param joinPoint AspectJ join point containing method signature, arguments, and execution context
     * @return Original method return value with validation applied, or validation exception
     * @throws ConstraintViolationException when validation rules are violated
     * @throws IllegalStateException when required context (ServerWebExchange) is missing
     */
    @Around("@annotation(io.ghaylan.springboot.validation.integration.ValidateInput) && within(@org.springframework.web.bind.annotation.RestController *)")
    open fun run(joinPoint: ProceedingJoinPoint): Any?
    {
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