package io.ghaylan.springboot.validation.utils

import io.ghaylan.springboot.validation.integration.ValidateInput
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

/**
 * Scans the [RequestMappingHandlerMapping] at startup to find all controller methods
 * annotated with [ValidateInput], returning a map of method to annotation.
 */
object ValidatedMethodFinder {
	
	/** Returns all request-mapped methods that carry a [ValidateInput] annotation. */
	fun find(
		appContext: ApplicationContext,
	): Map<Method, ValidateInput> {
		val handlerMapping: RequestMappingHandlerMapping = appContext.getBean<RequestMappingHandlerMapping>("requestMappingHandlerMapping")
		val result = HashMap<Method, ValidateInput>(handlerMapping.handlerMethods.size)
		
		for (handlerMethod in handlerMapping.handlerMethods.values) {
			result[handlerMethod.method] = handlerMethod.method.getAnnotation(ValidateInput::class.java)
				?: continue
		}
		
		return result
	}
}