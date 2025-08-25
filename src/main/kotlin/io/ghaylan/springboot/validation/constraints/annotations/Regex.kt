package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.regex.RegexConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.regex.RegexValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence matches the specified regular expression.
 *
 * ### Description
 * The `@Regex` annotation ensures that the annotated value (or each element in a collection)
 * conforms exactly to the regular expression pattern defined by the `expression` parameter.
 * This validation is useful for enforcing string formats such as codes, identifiers, or any
 * custom pattern constraints required by the business logic.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation fails if the value does not match the regular expression pattern.
 * - For arrays or collections, each element must individually match the regular expression.
 *
 * ### Example Usage
 * ```kotlin
 * @Regex(expression = "^[a-zA-Z0-9]{8}$")
 * val accessCode: String
 * // ✅ accessCode must be exactly 8 alphanumeric characters
 *
 * -----
 *
 * @Regex(expression = "^\\d{4}-\\d{2}-\\d{2}$")
 * val dates: List<String>
 * // ✅ each date in dates must match the format YYYY-MM-DD
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PATTERN_VIOLATION]
 *   Triggered when a value does not match the specified regular expression.
 *
 * @property pattern The regular expression pattern that the annotated value must match.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = RegexConstraint::class, validatedBy = [RegexValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Regex(
    val pattern : String,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])