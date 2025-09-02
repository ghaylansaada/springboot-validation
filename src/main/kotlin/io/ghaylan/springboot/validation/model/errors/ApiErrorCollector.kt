package io.ghaylan.springboot.validation.model.errors

import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.ext.toLanguageTagString
import io.ghaylan.springboot.validation.ext.extractLanguage
import io.ghaylan.springboot.validation.ext.normalizeLanguageTag
import java.util.Locale

/**
 * Collector for validation errors with locale-aware message resolution.
 *
 * This class provides a **fluent builder-style API** for adding validation errors
 * across different locations in an HTTP request (body, query, header, path) or business logic.
 *
 * Builders are automatically finalized when errors are inspected or thrown, so there is
 * **no need to call `.build()` manually**.
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
 * collector.body(ApiErrorCode.EMAIL_FORMAT_VIOLATION)
 *     .field("user.email")
 *     .data("attemptedValue", "not-an-email")
 *     .data("maxLength", 50)
 *     .msg("en", "Email is required")
 *     .msg("fr", "L'email est requis")
 *
 * // Throws ConstraintViolationException with best matching message if locale is en-US
 * collector.throwIfNotEmpty()
 * ```
 *
 * Once [throwIfNotEmpty] is called, all builders are finalized:
 * - Messages are resolved to [ApiError.message] based on the collector locale
 * - Internal message maps are cleared
 * - Returned [ApiError] instances are immutable
 *
 * @property locale The locale to use for message resolution
 */
class ApiErrorCollector(private val locale : Locale)
{
    private val errorsBuilder = mutableListOf<ApiErrorBuilder>()

    /**
     * Adds a business logic validation error.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     */
    fun business() = add(ApiError.ErrorLocation.BUSINESS)

    /**
     * Adds a request body validation error.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     */
    fun body() = add(ApiError.ErrorLocation.BODY)

    /**
     * Adds a query parameter validation error.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     */
    fun query() = add(ApiError.ErrorLocation.QUERY)

    /**
     * Adds an HTTP header validation error.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     */
    fun header() = add(ApiError.ErrorLocation.HEADER)

    /**
     * Adds a path variable validation error.
     * @param code Error code enum such as "REQUIRED", "INVALID", or custom codes
     */
    fun path() = add(ApiError.ErrorLocation.PATH)

    /**
     * Internal helper to add errors with the specified location.
     */
    private fun add(location: ApiError.ErrorLocation) : ApiErrorBuilder
    {
        val errorBuilder = ApiErrorBuilder(location)
        errorsBuilder.add(errorBuilder)
        return errorBuilder
    }


    /**
     * Throws [ConstraintViolationException] if any errors have been collected.
     *
     * All builders are automatically finalized, localized messages are resolved
     * according to the collector locale, and internal message maps are cleared.
     *
     * @throws ConstraintViolationException if any validation errors exist
     */
    fun throwIfNotEmpty()
    {
        if (errorsBuilder.isEmpty()) return

        throw ConstraintViolationException(errors = all())
    }


    /**
     * Returns a list of all collected errors, finalizing the builders and resolving messages
     */
    private fun all(): List<ApiError>
    {
        val size = errorsBuilder.size
        if (size == 0) return emptyList()

        val contextLang = locale.toLanguageTagString().normalizeLanguageTag()
        val contextLangShort = contextLang.extractLanguage()
        val allErrors = ArrayList<ApiError>(size)

        for (builder in errorsBuilder)
        {
            val error = builder.build()

            error.message = if (error.messages != null)
            {
                error.messages?.get(contextLang)?.takeIf { it.isNotEmpty() }
                    ?: error.messages?.get(contextLangShort)?.takeIf { it.isNotEmpty() }
                    ?: error.messages?.get("en")?.takeIf { it.isNotEmpty() }
                    ?: error.messages?.values?.firstOrNull { it.isNotEmpty() }
                    ?: error.message
            }
            else error.message

            error.messages = null

            allErrors.add(error)
        }

        return allErrors
    }
}