package io.ghaylan.springboot.validation.model.errors

/**
 * DSL builder for creating localized error messages.
 *
 * Used within [ApiErrorBuilder.messages] to add multiple language translations
 * for error messages. Supports both exact locale codes (e.g., "en-US") and
 * language-only codes (e.g., "en", "fr").
 *
 * Example usage with the DSL:
 * ```kotlin
 * val error = ApiErrorBuilder(ErrorLocation.BODY)
 *     .field("user.email")
 *     .code("INVALID_FORMAT")
 *     .messages {
 *         en("Invalid email format")
 *         fr("Format d'email invalide")
 *         ar("تنسيق البريد الإلكتروني غير صالح")
 *         add("es", "Formato de correo electrónico no válido")
 *     }
 *     .build()
 *
 * // The resulting messages map:
 * // messages = mapOf(
 * //   "en" to "Invalid email format",
 * //   "fr" to "Format d'email invalide",
 * //   "ar" to "تنسيق البريد الإلكتروني غير صالح",
 * //   "es" to "Formato de correo electrónico no válido"
 * // )
 * ```
 */
class ErrorMessagesBuilder
{
    private val map = mutableMapOf<String, String>()


    /**
     * Adds a localized message for the specified language or locale.
     *
     * @param language Language or locale code (e.g., "en", "fr", "en-US")
     * @param text The localized error message text
     */
    fun add(language: String, text: String) { map[language] = text }

    /**
     * Adds a localized message for Arabic language.
     *
     * @param text The error message
     */
    fun ar(text: String) { add("ar", text) }

    /**
     * Adds a localized message for English language.
     *
     * @param text The error message
     */
    fun en(text: String) { add("en", text) }

    /**
     * Adds a localized message for French language.
     *
     * @param text The error message
     */
    fun fr(text: String) { add("fr", text) }

    /**
     * Adds a localized message for Tunisian Arabic language.
     *
     * @param text The error message
     */
    fun arTN(text: String) { add("ar-TN", text) }


    /**
     * Builds and returns an immutable map of the configured messages.
     *
     * @return Immutable map of language/locale codes to localized messages
     */
    fun build(): Map<String, String> = map.toMap()
}