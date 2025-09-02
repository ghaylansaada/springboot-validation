package io.ghaylan.springboot.validation.model.errors

import io.ghaylan.springboot.validation.utils.PropNameAccessor
import java.lang.reflect.Field
import kotlin.reflect.KProperty

/**
 * Builder for constructing [ApiError] instances using a fluent and readable API.
 *
 * Provides a convenient way to create validation or business errors with:
 * - Optional field path references
 * - Additional contextual data
 * - Multi-language messages
 *
 * @property location The location of the error, e.g., query, header, path, body, or business
 * @property code The specific error code representing the type of validation or business error
 *
 * ## Example Usage
 * ```kotlin
 * val error = ApiErrorBuilder(ErrorLocation.BODY, ApiErrorCode.EMAIL_FORMAT_VIOLATION)
 *     .field("user.email")
 *     .data("attemptedValue", "not-an-email")
 *     .data("maxLength", 50)
 *     .msgEnglish("Invalid email format")
 *     .msgFrench("Format d'email invalide")
 *     .build()
 *
 * // Resulting ApiError:
 * // field = "user.email"
 * // code = "EMAIL_FORMAT_VIOLATION"
 * // message = null
 * // location = ErrorLocation.BODY
 * // data = mapOf("attemptedValue" to "not-an-email", "maxLength" to 50)
 * // messages = mapOf("en" to "Invalid email format", "fr" to "Format d'email invalide")
 * ```
 */
class ApiErrorBuilder(private val location: ApiError.ErrorLocation)
{
    private var code: Enum<*>? = null
    private var field: String? = null
    private var data: Any? = null
    private var dataMap: HashMap<String, Any?>? = null
    private var messagesMap = HashMap<String, String>()

    /**
     * Sets the error code for this [ApiErrorBuilder].
     *
     * The error code identifies the type of validation or business error.
     * This can be any enum representing codes such as "REQUIRED", "INVALID", or custom codes.
     *
     * @param code The enum value representing the error code
     * @return This [ApiErrorBuilder] instance for chaining
     */
    fun code(code: Enum<*>) : ApiErrorBuilder
    {
        this.code = code
        return this
    }


    /**
     * Sets the field path for the error using a dot-notation string.
     *
     * @param field Dot-notation path to the invalid field
     *              e.g., "user.name" or "items[0].price"
     * @return This builder instance for chaining
     */
    fun field(field: String) : ApiErrorBuilder
    {
        this.field = field
        return this
    }

    /**
     * Sets the field path for the error using a Kotlin property reference.
     *
     * @param property KProperty of the invalid field (e.g., User::name, Item::price)
     * @return This builder instance for chaining
     */
    fun field(property: KProperty<*>) : ApiErrorBuilder
    {
        this.field = PropNameAccessor.getName(property)
        return this
    }

    /**
     * Sets the field path for the error using a Java reflection field.
     *
     * @param field The Java [Field] representing the invalid property
     * @return This builder instance for chaining
     */
    fun field(field: Field) : ApiErrorBuilder
    {
        this.field = PropNameAccessor.getName(field)
        return this
    }


    /**
     * Sets a generic data object associated with this error.
     *
     * @param value Arbitrary data object to include in the error
     * @return This builder instance for chaining
     */
    fun data(value: Any?): ApiErrorBuilder
    {
        this.data = value
        return this
    }

    /**
     * Adds a single key-value pair to the data map associated with this error.
     *
     * @param key The key identifying the piece of data
     * @param value The value associated with the key
     * @return This builder instance for chaining
     */
    fun data(key: String, value: Any?): ApiErrorBuilder
    {
        if (dataMap == null) dataMap = HashMap()
        dataMap!![key] = value
        return this
    }


    /**
     * Sets the English message for this error.
     *
     * @param message The error message in English
     * @return This builder instance for chaining
     */
    fun msgEnglish(message: String): ApiErrorBuilder
    {
        messagesMap["en"] = message
        return this
    }

    /**
     * Sets the French message for this error.
     *
     * @param message The error message in French
     * @return This builder instance for chaining
     */
    fun msgFrench(message: String): ApiErrorBuilder
    {
        messagesMap["fr"] = message
        return this
    }

    /**
     * Sets the Arabic message for this error.
     *
     * @param message The error message in Arabic
     * @return This builder instance for chaining
     */
    fun msgArabic(message: String): ApiErrorBuilder
    {
        messagesMap["ar"] = message
        return this
    }

    /**
     * Sets the Tunisian Arabic message for this error.
     *
     * @param message The error message in Tunisian Arabic
     * @return This builder instance for chaining
     */
    fun msgTunisian(message: String): ApiErrorBuilder
    {
        messagesMap["ar-TN"] = message
        return this
    }

    /**
     * Sets a message for this error in any custom language.
     *
     * @param language Language code (e.g., "en", "fr", "ar-TN")
     * @param message The error message in the specified language
     * @return This builder instance for chaining
     */
    fun msg(language: String, message: String): ApiErrorBuilder
    {
        messagesMap[language] = message
        return this
    }


    /**
     * Builds and returns the configured [ApiError] instance.
     * @return The constructed ApiError with all configured properties
     */
    internal fun build(): ApiError = ApiError(
        path = field,
        code = code,
        message = null,
        location = location,
        data = dataMap?.ifEmpty { null } ?: data,
    ).also { it.messages = messagesMap }
}