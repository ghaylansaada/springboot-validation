package io.ghaylan.springboot.validation.utils

import io.ghaylan.springboot.validation.integration.ValidateInput
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method
import kotlin.jvm.java

/**
 * Utility for discovering controller methods annotated with [ValidateInput].
 *
 * This is typically invoked at application startup to collect all methods
 * that require request validation and build their corresponding schemas.
 *
 * It inspects the [RequestMappingHandlerMapping] registered in the
 * application context and extracts the mapping between:
 *
 * - **Controller method** (`Method`)
 * - **Validation configuration** ([ValidateInput] annotation)
 *
 * ### Typical Use Cases
 * - Build validation schemas for all annotated endpoints at startup
 * - Centralized registry of validated methods
 * - Enforce consistent request validation
 *
 * ### Example
 * ```kotlin
 * val targets = ValidateInputScanner.find(appContext)
 * targets.forEach { (method, annotation) ->
 *     println("Discovered validation target: ${method.declaringClass.simpleName}#${method.name}")
 * }
 * ```
 */
object ValidatedMethodFinder
{

    /**
     * Finds all controller methods annotated with [ValidateInput].
     *
     * @param appContext Spring application context containing WebFlux configuration
     * @return Map of controller [Method] to its [ValidateInput] annotation
     *
     * ### Notes
     * - Only methods in request-mapped controllers are scanned.
     * - Methods without [ValidateInput] are ignored.
     */
    fun find(
        appContext : ApplicationContext,
    ): Map<Method, ValidateInput>
    {
        val handlerMapping = appContext.getBean(RequestMappingHandlerMapping::class.java)

        val result = HashMap<Method, ValidateInput>(handlerMapping.handlerMethods.size)

        for (handlerMethod in handlerMapping.handlerMethods.values)
        {
            result[handlerMethod.method] = handlerMethod.method.getAnnotation(ValidateInput::class.java) ?: continue
        }

        return result
    }
}