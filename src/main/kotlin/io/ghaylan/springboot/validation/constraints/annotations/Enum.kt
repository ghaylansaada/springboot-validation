package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.enums.EnumConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.enums.EnumValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence follows the naming format of a Java/Kotlin enum constant.
 *
 * ### Description
 * The `@Enum` annotation ensures that the annotated value (or each element in a collection)
 * matches the conventional enum constant naming style:
 * - Uppercase letters
 * - Digits (optional, but not at the start)
 * - Underscores as separators
 *
 * This constraint is useful for validating user input that must match specific enum constant names
 * or ensuring that persisted string values can be safely mapped to enum constants.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation fails if:
 *   - The first character is not an uppercase letter (`A-Z`)
 *   - The string contains invalid characters (anything other than `A-Z`, `0-9`, or `_`)
 * - When `ignoreCase` is `true`, the comparison is case-insensitive but still enforces the allowed characters.
 * - For arrays or collections, each element must individually match the enum naming format.
 *
 * ### Example Usage
 * ```kotlin
 * @Enum
 * val status: String
 * // ✅ status must match enum naming style (e.g., "PENDING", "APPROVED_1", "IN_PROGRESS")
 *
 * -----
 *
 * @Enum(ignoreCase = true)
 * val permissions: List<CharSequence>
 * // ✅ each value in permissions must be valid enum-style text (case-insensitive)
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.ENUM_FORMAT_VIOLATION]
 *   Triggered when a value does not follow the enum constant naming format.
 *
 * @property ignoreCase Whether to ignore character case when validating the enum constant name. Defaults to `false`.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = EnumConstraint::class, validatedBy = [EnumValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Enum(
    val ignoreCase: Boolean = false,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])