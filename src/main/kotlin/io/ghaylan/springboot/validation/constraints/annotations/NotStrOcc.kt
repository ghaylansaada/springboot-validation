package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.notcontains.NotStrOccConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.notcontains.NotStrOccValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence **does not** contain a specified substring
 * according to defined matching mode rules.
 *
 * ### Description
 * The `@NotStrOcc` annotation ensures that the annotated `String` or `CharSequence` value (or each element in a collection)
 * does **not** satisfy the specified substring presence constraint.
 * Validation modes control whether the annotated value must **not** equal, contain, start with, or end with
 * the specified substring.
 *
 * This is useful for enforcing negative substring presence rules, such as disallowing certain tokens,
 * prefixes, suffixes, or exact matches within text fields.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The annotated value must **not** satisfy the mode-based check against the specified substring:
 *      - `EQUALS`: the value must not exactly match the substring.
 *      - `CONTAINS`: the value must not contain the substring.
 *      - `STARTS_WITH`: the value must not start with the substring.
 *      - `ENDS_WITH`: the value must not end with the substring.
 * - Matching respects the `ignoreCase` flag for case-insensitive checks.
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, allowing multiple negative constraints with different substrings
 * or modes to be applied on the same field/parameter.
 *
 * ### Example Usage
 * ```kotlin
 * @NotStrOcc(value = "abc", mode = StrOccMode.CONTAINS)
 * val text: String
 * // ✅ text must not contain "abc" (case-insensitive by default)
 *
 * -----
 *
 * @NotStrOcc(value = "prefix", mode = StrOccMode.STARTS_WITH, ignoreCase = false)
 * val code: String
 * // ✅ code must not start with "prefix" (case-sensitive)
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.STRING_OCCURRENCES_VIOLATION]
 *   Triggered when the substring presence constraint is violated (i.e., the substring was found
 *   when it should not be).
 *
 * @property value The substring to check for absence within the annotated value.
 * @property ignoreCase Whether to ignore case when matching the substring.
 * @property mode The mode defining how the substring must not relate to the annotated value.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = NotStrOccConstraint::class, validatedBy = [NotStrOccValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NotStrOcc(
    val value : String,
    val ignoreCase : Boolean = true,
    val mode : StrOcc.StrOccMode = StrOcc.StrOccMode.EQUALS,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])