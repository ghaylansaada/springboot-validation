package io.ghaylan.springboot.validation.model.errors

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
@Schema(name = "ApiError", description = "Detailed error entry for failed API responses.")
data class ApiError(

    @field:Schema(
        description = "Fully qualified path of the invalid field relative to the request model. Null if the error is not tied to a specific parameter.",
        nullable = true,
        examples = [
            "email",
            "user.profile.firstName",
            "orders[0].id",
            "orders[0].items[2].productId",
            "contacts[0].phones[1].number",
            "metadata.tags[3]"])
    val field: String? = null,

    @field:Schema(
        description = "Classification of the error.",
        nullable = true,
        enumAsRef = true,
        implementation = ApiErrorCode::class)
    val code: Enum<*>? = null,

    @field:Schema(
        description = "Localized, human-readable error message.",
        example = "City is required",
        nullable = true)
    var message: String? = null,

    @field:Schema(
        description = "Location in the request where the invalid value originated.",
        nullable = true,
        enumAsRef = true,
        implementation = ErrorLocation::class)
    val location: ErrorLocation? = null,

    @field:Schema(
        description = "Optional additional context data for the error.",
        example = """{ "minLength": 3, "maxLength": 50 }""",
        nullable = true)
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