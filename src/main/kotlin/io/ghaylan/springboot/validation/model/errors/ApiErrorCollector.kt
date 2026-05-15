package io.ghaylan.springboot.validation.model.errors

import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException

/**
 * Fluent collector for building and aggregating [ApiError] instances across request sections.
 *
 * ```kotlin
 * val collector = ApiErrorCollector()
 *
 * collector.body(ApiErrorCode.EMAIL_FORMAT_VIOLATION)
 *     .field("user.email")
 *     .message("Email is required")
 *
 * collector.throwIfNotEmpty()
 * ```
 */
class ApiErrorCollector {
	
	private val errorsBuilder = mutableListOf<ApiErrorBuilder>()
	
	/**
	 * Adds a business logic validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun business(code: Enum<*>) = add(ApiError.ErrorLocation.BUSINESS, code)
	
	/**
	 * Adds a request body validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun body(code: Enum<*>) = add(ApiError.ErrorLocation.BODY, code)
	
	/**
	 * Adds a query parameter validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun query(code: Enum<*>) = add(ApiError.ErrorLocation.QUERY, code)
	
	/**
	 * Adds an HTTP header validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun header(code: Enum<*>) = add(ApiError.ErrorLocation.HEADER, code)
	
	/**
	 * Adds a path variable validation error.
	 * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
	 */
	fun path(code: Enum<*>) = add(ApiError.ErrorLocation.PATH, code)
	
	/**
	 * Internal helper to add errors with the specified location.
	 */
	private fun add(
		location: ApiError.ErrorLocation,
		code: Enum<*>
	): ApiErrorBuilder {
		val errorBuilder = ApiErrorBuilder(location, code)
		errorsBuilder.add(errorBuilder)
		return errorBuilder
	}
	
	/**
	 * Throws [ConstraintViolationException] if any errors have been collected.
	 */
	fun throwIfNotEmpty() {
		if (errorsBuilder.isEmpty()) return
		throw ConstraintViolationException(errors = collect())
	}
	
	/** Finalizes all builders and returns the collected errors. */
	fun collect(): List<ApiError> {
		val size = errorsBuilder.size
		if (size == 0) return emptyList()
		val allErrors = ArrayList<ApiError>(size)
		
		for (builder in errorsBuilder) {
			allErrors.add(builder.build())
		}
		
		return allErrors
	}
}