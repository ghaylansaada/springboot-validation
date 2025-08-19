package io.ghaylan.springboot.validation.model.errors

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
 * - `"user.address.city"`
 * - `"phones[0].number"`
 * - `"address[0][0].latitude"`
 * - `"[0].name"`
 * - `"[0][0].name"`
 * - `"colors[0][0]"`
 * - `"colors[0]"`
 * - `"id"`
 *
 * @property field Fully qualified path of the invalid field.
 * @property message Localized, human-readable error message.
 * @property code Classification of the error (e.g., `REQUIRED`, `INVALID`).
 * @property location Location in the request where the invalid value originated.
 * @property data Optional additional context data for the error.
 */
data class ApiError(
    val field: String? = null,
    val code: Enum<*>? = null,
    var message: String? = null,
    val location: ErrorLocation? = null,
    val data : Any? = null)
{
    /**
     * Internal map of localized messages keyed by language code.
     * Used during message resolution before being cleared.
     */
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