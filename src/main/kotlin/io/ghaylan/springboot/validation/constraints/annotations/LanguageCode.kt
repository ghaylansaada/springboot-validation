package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.language.LanguageConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.language.LanguageValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence represents a valid ISO language code.
 *
 * ### Description
 * The `@LanguageCode` annotation ensures that the annotated value (or each element in a collection)
 * matches a valid ISO language tag in the format defined by [java.util.Locale].
 * Specifically, it supports:
 * - Two-letter ISO 639-1 language codes (e.g., "en", "fr", "ar")
 * - Optionally followed by a hyphen and a two-letter ISO 3166-1 country code (e.g., "en-US", "fr-CA")
 *
 * This validation is useful for fields representing language identifiers, locale settings,
 * or any data that must conform to standard language tags.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation passes if the value matches the pattern `"xx"` or `"xx-YY"` where:
 *    - `xx` is a valid ISO 639-1 language code (2 lowercase letters)
 *    - `YY` is a valid ISO 3166-1 country code (2 uppercase letters), optional
 * - For arrays or collections, each element must individually be a valid language code.
 *
 * ### Example Usage
 * ```kotlin
 * @LanguageCode
 * val language: String
 * // ✅ language must be a valid ISO language code such as "en" or "fr-CA"
 *
 * -----
 *
 * @LanguageCode
 * val supportedLanguages: List<String>
 * // ✅ each entry must be a valid ISO language code like "es", "de-DE", etc.
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.ISO_LANGUAGE_CODE_VIOLATION]
 *   Triggered when a value does not conform to the required ISO language code format.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = LanguageConstraint::class, validatedBy = [LanguageValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class LanguageCode(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])