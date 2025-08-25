package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.country.ISOCountryConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.country.ISOCountryValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence is a valid ISO 3166-1 country code.
 *
 * ### Description
 * The `@ISOCountryCode` annotation ensures that the annotated value (or each element in a collection)
 * matches one of the officially recognized **two-letter country codes** as defined by the ISO 3166-1 standard.
 * This is useful for validating country fields in user profiles, addresses, or location-based data.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The value must exactly match a two-letter ISO 3166-1 alpha-2 code (case-sensitive).
 * - For arrays or collections, each element must individually meet the above rule.
 *
 * ### Example Usage
 * ```kotlin
 * @ISOCountryCode
 * val country: String
 * // ✅ Must be a valid ISO 3166-1 alpha-2 code (e.g., "US", "FR", "JP")
 *
 * -----
 *
 * @ISOCountryCode
 * val countries: List<CharSequence>
 * // ✅ Each value must be a valid ISO 3166-1 alpha-2 code
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.ISO_COUNTRY_CODE_VIOLATION]
 *   Triggered when a value is not found in the list of valid ISO 3166-1 alpha-2 country codes.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = ISOCountryConstraint::class, validatedBy = [ISOCountryValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ISOCountryCode(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])