package io.ghaylan.springboot.validation.model.errors

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a validation error returned to the API caller.
 *
 * Describes a specific validation failure in an incoming request. Each instance identifies
 * **where** the error came from (query/header/path/body), **which field** (path) is affected,
 * a **localized human-readable message**, and an **error classification**.
 *
 * ### Field path format
 * Use a fully qualified, dot/bracket path relative to the logical request model, not the raw wire format.
 * Examples:
 * - `"field"`
 * - `"field.nested"`
 * - `"field[0]"`
 * - `"field[0][1]"`
 * - `"field[0].nested"`
 * - `"field[0][1].nested"`
 * - `"[0]field"
 * - `"[0][1].nested"`
 *
 * @property path Fully qualified path of the invalid field.
 * @property message Localized, human-readable error message.
 * @property code Classification of the error (e.g., `REQUIRED`, `INVALID`).
 * @property location Location in the request where the invalid value originated.
 * @property data Optional additional context data for the error.
 */
@Schema(name = "ApiError", description = "Detailed error entry for failed API request.")
data class ApiError(

    @Schema(
        description = """
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
            """,
        nullable = true,
        example = "param")
    val path: String? = null,

    @Schema(
        description = "Classification of the error.",
        nullable = true,
        enumAsRef = true,
        example = "REQUIRED_VIOLATION",
        implementation = ApiErrorCode::class)
    val code: Enum<*>? = null,

    @Schema(
        description = "Localized, human-readable error message.",
        example = "Param is required",
        nullable = true)
    var message: String? = null,

    @Schema(
        description = "Location in the request where the invalid value originated.",
        nullable = true,
        enumAsRef = true,
        example = "QUERY",
        implementation = ErrorLocation::class)
    val location: ErrorLocation? = null,

    @Schema(
        description = "Optional additional context data for the error.",
        example = """{ "key": "value" }""",
        nullable = true)
    val data : Any? = null)
{
    /**
     * Internal map of localized messages keyed by language code.
     * Used during message resolution before being cleared.
     */
    @Schema(hidden = true)
    @JsonIgnore
    var messages : Map<String, String>? = null



    /**
     * Lists the high-level location within an HTTP request that produced a validation error.
     *
     * Use these to help clients decide how to highlight or map errors back to UI inputs.
     */
    enum class ErrorLocation
    {
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