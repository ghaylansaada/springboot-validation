package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuenotin.ValueNotInConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuenotin.ValueNotInValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated scalar value or all elements in the array/list are **excluded** from a predefined set of disallowed values.
 *
 * ### Description
 * The `@ValueNotIn` annotation ensures that the value of the annotated field or parameter does **not** match any of the string values defined in the `values` list.
 * It supports both single scalar values and collections/arrays of scalar values, enforcing that **none of the elements** match any of the provided `values`.
 *
 * This is useful for rejecting forbidden values in user input, such as blocked roles, invalid states, disallowed categories, or deprecated codes.
 *
 * ### Supported Types
 * - Scalar types: `String`, `Int`, `Long`, `Double`, `Float`, `BigDecimal`, `LocalDate`, `LocalDateTime`, `Date`, etc.
 * - Collections and arrays of the above scalar types (e.g., `List<String>`, `Set<Int>`, `Array<LocalDate>`)
 * - Any type with a meaningful `toString()` representation that can match a value in the list
 *
 * ### Validation Rules
 * - The annotated value (or each element, if it's a collection/array) is converted to a string and checked against the provided `values`.
 * - Validation **passes** if:
 *      - The value is `null`
 *      - For scalar types: the string value is **not** included in the `values` list
 *      - For collections/arrays: **none of the elements** are included in the `values` list (after string conversion)
 *
 * ### Example Usage
 * ```kotlin
 * @ValueNotIn(values = ["BANNED", "LOCKED", "DEACTIVATED"])
 * val accountStatus: String
 * // ✅ Ensures `accountStatus` is not one of the blocked statuses
 *
 * -----
 *
 * @ValueNotIn(values = ["X", "Y", "Z"])
 * val tags: List<String>
 * // ✅ Ensures no entry in `tags` is one of X, Y, or Z
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.DISALLOWED_VALUE_VIOLATION]
 *   Triggered when a value (or an element in an array/list) **matches** one of the disallowed `values`.
 *
 * @property values The list of disallowed string values. Scalar values (or all elements in an array/list) must **not** match any of these when converted to a string.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = ValueNotInConstraint::class, validatedBy = [ValueNotInValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ValueNotIn(
    val values : Array<String>,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])