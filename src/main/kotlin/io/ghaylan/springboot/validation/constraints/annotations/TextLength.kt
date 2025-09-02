package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.length.TextLengthConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.length.TextLengthValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence's length falls within a specified range.
 *
 * ### Description
 * The `@TextLength` annotation ensures that the annotated string or character sequence
 * has a length between the specified `min` and `max` values, inclusive.
 * This is useful for validating input fields where length constraints must be enforced,
 * such as usernames, descriptions, or any text-based parameters.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation passes if the length of the value is between `min` and `max` (inclusive).
 * - Validation fails if the length is outside the specified range.
 * - For arrays or collections, each element must individually meet the length constraints.
 *
 * ### Example Usage
 * ```kotlin
 * @TextLength(min = 3, max = 50)
 * val username: String
 * // ✅ username must contain between 3 and 50 characters
 *
 * -----
 *
 * @TextLength(min = 0, max = 255)
 * val email: String
 * // ✅ email must contain between 0 and 255 characters
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.STRING_LENGTH_VIOLATION]
 *   Triggered when the length of the string is not within the specified bounds.
 *
 * @property min The minimum allowed length for the string (inclusive).
 * @property max The maximum allowed length for the string (inclusive).
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = TextLengthConstraint::class, validatedBy = [TextLengthValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class TextLength(
    val min : Int,
    val max : Int,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])