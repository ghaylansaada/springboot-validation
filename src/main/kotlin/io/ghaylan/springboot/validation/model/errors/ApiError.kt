package io.ghaylan.springboot.validation.model.errors

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a single validation error returned to the API caller.
 *
 * @property path Dot/bracket path of the invalid field (e.g., `"user.address[0].city"`).
 * @property code Error classification (e.g., [ApiErrorCode.REQUIRED_VIOLATION]).
 * @property message Optional human-readable error message.
 * @property location Request section where the error originated (body, query, header, path, or business).
 * @property data Optional additional context data.
 */
@Schema(name = "ApiError", description = "Detailed error entry for failed API request.")
data class ApiError(
	@Schema(description = """
            Fully qualified path of the invalid parameter relative to the request model. 
            Null if the error is not tied to a specific parameter.
            Examples:
            - `"field"`
            - `"field.nested"`
            - `"field[0]"`
            - `"field[0][1]"`
            - `"field[0].nested"`
            - `"field[0][1].nested"`
            - `"[0]nested"
            - `"[0][1].nested"`
            """, nullable = true, example = "param")
	val path: String? = null,
	@Schema(description = "Classification of the error.", enumAsRef = true, example = "REQUIRED_VIOLATION", implementation = ApiErrorCode::class)
	val code: Enum<*>? = null,
	@Schema(description = "Human-readable error message.", example = "Param is required", nullable = true)
	var message: String? = null,
	@Schema(description = "Location in the request where the invalid value originated.",
		enumAsRef = true,
		example = "QUERY",
		implementation = ErrorLocation::class)
	val location: ErrorLocation? = null,
	@Schema(description = "Optional additional context data for the error.", example = """{ "key": "value" }""", nullable = true)
	val data: Any? = null
) {
	
	/**
	 * Lists the high-level location within an HTTP request that produced a validation error.
	 *
	 * Use these to help clients decide how to highlight or map errors back to UI inputs.
	 */
	enum class ErrorLocation {
		
		/** Error in a URL query parameter. */
		QUERY,
		
		/** Error in an HTTP header value. */
		HEADER,
		
		/** Error in a URI path variable (templated segment). */
		PATH,
		
		/** Error in the request body payload (e.g., JSON, XML, form, multipart). */
		BODY,
		
		/** Error from business logic, not tied to a specific field. */
		BUSINESS
	}
}