package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.contains.StrOccConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.contains.StrOccValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence contains a specified substring according to
 * defined occurrence and matching mode rules.
 *
 * ### Description
 * The `@StrOcc` annotation ensures that the annotated `String` or `CharSequence` value (or each element in a collection)
 * satisfies constraints based on the presence and number of occurrences of a specific substring.
 * Validation modes control whether the annotated value must equal, contain, start with, or end with
 * the specified substring. Additionally, the number of occurrences of the substring is constrained
 * between a minimum and maximum count.
 *
 * This is useful for enforcing complex substring presence rules, such as ensuring certain tokens,
 * prefixes, suffixes, or exact matches within text fields.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The annotated value must satisfy the mode-based check against the specified substring:
 *      - `EQUALS`: the value must exactly match the substring.
 *      - `CONTAINS`: the value must contain the substring.
 *      - `STARTS_WITH`: the value must start with the substring.
 *      - `ENDS_WITH`: the value must end with the substring.
 * - The number of occurrences of the substring in the value must be between `minOccurrences` and `maxOccurrences`.
 * - Matching respects the `ignoreCase` flag for case-insensitive checks.
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, allowing multiple constraints with different divisors to be applied on the same field/parameter.
 *
 * ### Example Usage
 * ```kotlin
 * @StrOcc(value = "abc", minOccurrences = 1, maxOccurrences = 3, mode = StrOccMode.CONTAINS)
 * val text: String
 * // ✅ text must contain "abc" between 1 and 3 times (case-insensitive by default)
 *
 * -----
 *
 * @StrOcc(value = "prefix", mode = StrOccMode.STARTS_WITH, ignoreCase = false)
 * val code: String
 * // ✅ code must start with "prefix" (case-sensitive)
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.STRING_OCCURRENCES_VIOLATION]
 *   Triggered when the substring presence or occurrence constraints are violated.
 *
 * @property value The substring to check for within the annotated value.
 * @property minOccurrences The minimum number of times the substring must occur (inclusive).
 * @property maxOccurrences The maximum number of times the substring may occur (inclusive).
 * @property ignoreCase Whether to ignore case when matching the substring.
 * @property mode The mode defining how the substring must relate to the annotated value.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = StrOccConstraint::class, validatedBy = [StrOccValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class StrOcc(
    val value : String,
    val minOccurrences: Int = 1,
    val maxOccurrences: Int = Int.MAX_VALUE,
    val ignoreCase : Boolean = true,
    val mode : StrOccMode = StrOccMode.EQUALS,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])
{
    enum class StrOccMode
    {
        EQUALS,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }
}