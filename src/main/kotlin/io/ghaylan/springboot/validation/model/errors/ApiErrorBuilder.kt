package io.ghaylan.springboot.validation.model.errors

import kotlin.reflect.KProperty

/**
 * Builder for constructing [ApiError] instances using a fluent API.
 *
 * Provides a convenient way to build validation errors with optional localized messages.
 *
 * @param location The error location (query, header, path, body, or business)
 *
 * Example usage:
 * ```kotlin
 * val error = ApiErrorBuilder(ErrorLocation.BODY, ApiErrorCode.EMAIL_FORMAT_VIOLATION)
 *     .field("user.email")
 *     .data {
 *         add("attemptedValue", "not-an-email")
 *         add("maxLength", 50)
 *     }
 *     .messages {
 *         en("Invalid email format")
 *         fr("Format d'email invalide")
 *     }
 *     .build()
 *
 * // The resulting ApiError:
 * // field = "user.email"
 * // code = "EMAIL_FORMAT_VIOLATION"
 * // message = null
 * // location = ErrorLocation.BODY
 * // data = mapOf("attemptedValue" to "not-an-email")
 * // messages = mapOf("en" to "Invalid email format", "fr" to "Format d'email invalide")
 * ```
 */
class ApiErrorBuilder(private val location: ApiError.ErrorLocation, private val code : Enum<*>)
{
    private var field: String? = null
    private var data: Any? = null
    private var message : String? = null

    private var messagesMap: Map<String, String> = emptyMap()

    /**
     * Sets the field path for the error.
     * @param field Dot-notation path to the invalid field (e.g., "user.name", "items[0].price")
     */
    fun field(field: String) { this.field = field }


    /**
     * Sets the field path for the error.
     * @param field KProperty of the invalid field (e.g., User::name, Item::price)
     */
    fun field(field: KProperty<*>) { this.field = field.name }


    /**
     * Sets additional context data for the error.
     * @param data Any additional information that might be helpful for error handling
     */
    fun data(data: Any) { this.data = data }


    /**
     * Sets additional context data for the error.
     * @param block Lambda to configure messages using [ErrorDataBuilder]
     */
    fun data(block: ErrorDataBuilder.() -> Unit) {
        data = ErrorDataBuilder().apply(block).build()
    }


    /**
     * Sets the default error message.
     * @param message Default message to use if no localized message is available
     */
    fun message(message: String) { this.message = message }


    /**
     * Configures localized error messages using a DSL.
     * @param block Lambda to configure messages using [ErrorMessagesBuilder]
     */
    fun messages(block: ErrorMessagesBuilder.() -> Unit) {
        messagesMap = ErrorMessagesBuilder().apply(block).build()
    }


    /**
     * Builds and returns the configured [ApiError] instance.
     * @return The constructed ApiError with all configured properties
     */
    fun build(): ApiError = ApiError(
        field = field,
        code = code,
        message = null,
        location = location,
        data = data
    ).also { it.messages = messagesMap }
}