package io.ghaylan.springboot.validation.ext

import java.util.Locale

/**
 * Converts this [Locale] to a language string suitable for use in localization or language tags.
 *
 * If the locale has a country, the result is in the format "language_COUNTRY" (e.g., "en_US"),
 * otherwise just "language" (e.g., "en"). The resulting string is always lowercase.
 *
 * Example:
 * ```
 * Locale("en", "US").toLanguageTagString() // "en_us"
 * Locale("fr").toLanguageTagString()       // "fr"
 * ```
 */
internal fun Locale.toLanguageTagString() : String
{
    return when {
        this.country.isNotEmpty() -> "${this.language}_${this.country}"
        else -> this.language
    }.lowercase()
}

/**
 * Normalizes a language string by converting it to lowercase and replacing underscores with hyphens.
 *
 * Example:
 * ```
 * "en_US".normalizeLanguageTag() // "en-us"
 * "FR".normalizeLanguageTag()    // "fr"
 * ```
 */
internal fun String.normalizeLanguageTag() : String = this.lowercase().replace("_", "-")

/**
 * Extracts only the language part from a normalized language string.
 *
 * Returns the substring before the first hyphen, ignoring any country or region part.
 *
 * Example:
 * ```
 * "en-us".extractLanguage() // "en"
 * "fr".extractLanguage()    // "fr"
 * ```
 */
internal fun String.extractLanguage() : String = this.substringBefore("-")