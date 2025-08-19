package io.ghaylan.springboot.validation.model.errors

import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.ext.toLanguageTagString
import io.ghaylan.springboot.validation.ext.extractLanguage
import io.ghaylan.springboot.validation.ext.normalizeLanguageTag
import java.util.Locale

/**
 * Collector for validation errors with locale-aware message resolution.
 *
 * This class provides a fluent DSL for adding validation errors across different locations
 * in an HTTP request (body, query, header, path) or business logic. It automatically
 * resolves the best matching localized message when throwing a [ConstraintViolationException].
 *
 * **Message resolution priority:**
 * 1. Exact locale match (language + country, e.g., "en-US")
 * 2. Language-only match (e.g., "en" for "en-US")
 * 3. English fallback ("en")
 *
 * Example usage:
 * ```kotlin
 * val collector = ApiErrorCollector(Locale.ENGLISH)
 *
 * collector.body {
 *     field("user.email")
 *     code(ApiErrorCode.EMAIL_FORMAT_VIOLATION)
 *     .data {
 *         add("attemptedValue", "not-an-email")
 *         add("maxLength", 50)
 *     }
 *     messages {
 *         en("Email is required")
 *         fr("L'email est requis")
 *         add("en-US", "Email is required (US)")
 *     }
 * }
 *
 * // Throws ConstraintViolationException with US English message if locale is en-US
 * collector.throwIfNotEmpty()
 * ```
 *
 * Once [throwIfNotEmpty] is called, all localized messages are resolved to [ApiError.message],
 * and the internal [ApiError.messages] map is cleared.
 *
 * @property locale The locale to use for message resolution
 */
class ApiErrorCollector(private val locale : Locale)
{
    private val errors = mutableListOf<ApiError>()

    /**
     * Adds a business logic validation error using DSL.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     * @param block Configuration block for the error using [ApiErrorBuilder]
     */
    fun business(code: Enum<*>, block: ApiErrorBuilder.() -> Unit) = add(ApiError.ErrorLocation.BUSINESS, code, block)

    /**
     * Adds a request body validation error using DSL.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     * @param block Configuration block for the error using [ApiErrorBuilder]
     */
    fun body(code: Enum<*>, block: ApiErrorBuilder.() -> Unit) = add(ApiError.ErrorLocation.BODY, code, block)

    /**
     * Adds a query parameter validation error using DSL.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     * @param block Configuration block for the error using [ApiErrorBuilder]
     */
    fun query(code: Enum<*>, block: ApiErrorBuilder.() -> Unit) = add(ApiError.ErrorLocation.QUERY, code, block)

    /**
     * Adds an HTTP header validation error using DSL.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     * @param block Configuration block for the error using [ApiErrorBuilder]
     */
    fun header(code: Enum<*>, block: ApiErrorBuilder.() -> Unit) = add(ApiError.ErrorLocation.HEADER, code, block)

    /**
     * Adds a path variable validation error using DSL.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     * @param block Configuration block for the error using [ApiErrorBuilder]
     */
    fun path(code: Enum<*>, block: ApiErrorBuilder.() -> Unit) = add(ApiError.ErrorLocation.PATH, code, block)

    /**
     * Internal helper to add errors with the specified location.
     */
    private fun add(location: ApiError.ErrorLocation, code: Enum<*>, block: ApiErrorBuilder.() -> Unit) {
        val error = ApiErrorBuilder(location, code).apply(block).build()
        errors.add(error)
    }


    /**
     * Throws [ConstraintViolationException] if any errors have been collected.
     *
     * Resolves the best matching localized message for each error based on the provided locale,
     * assigns it to [ApiError.message], and clears the internal [ApiError.messages] map.
     *
     * @throws ConstraintViolationException if any validation errors exist
     */
    fun throwIfNotEmpty()
    {
        if (errors.isNotEmpty())
        {
            val contextLang = locale.toLanguageTagString().normalizeLanguageTag()
            val contextLangShort = contextLang.extractLanguage()

            for (error in errors)
            {
                // Select best match: exact -> partial -> English -> default
                error.message = error.messages?.get(contextLang)?.ifEmpty { null }
                    ?: error.messages?.get(contextLangShort)?.ifEmpty { null }
                    ?: error.messages?.get("en")?.ifEmpty { null }
                    ?: error.message

                error.messages = null
            }

            throw ConstraintViolationException(errors = errors)
        }
    }


    /**
     * Returns all collected errors as an immutable list.
     * @return List of all validation errors that have been collected
     */
    fun all(): List<ApiError> = errors.toList()
}