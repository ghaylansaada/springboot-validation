package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuein.ValueInConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuein.ValueInValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated scalar value or all elements in the array/list are included in a predefined set of allowed values.
 *
 * ### Description
 * The `@ValueIn` annotation ensures that the value of the annotated field or parameter is strictly limited to a specific set of allowed string values.
 * It supports both single scalar values and collections/arrays of scalar values, enforcing that **each element** must match one of the provided `values`.
 *
 * This is useful for enforcing domain restrictions on user input, such as allowed roles, categories, types, or constants — whether singular or repeated.
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
 *      - For scalar types: the string value is included in the `values` list
 *      - For collections/arrays: **all elements** are included in the `values` list (after string conversion)
 *
 * ### Example Usage
 * ```kotlin
 * @ValueIn(values = ["ADMIN", "USER", "GUEST"])
 * val role: String
 * // ✅ Ensures `role` is one of ADMIN, USER, or GUEST
 *
 * -----
 *
 * @ValueIn(values = ["A", "B", "C"])
 * val categories: List<String>
 * // ✅ Ensures every item in `categories` is one of A, B, or C
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.ALLOWED_VALUE_VIOLATION]
 *   Triggered when a value (or an element in an array/list) is not part of the allowed `values`.
 *
 * @property values The list of allowed string values. Scalar values (or all elements in an array/list) must match one of these when converted to a string.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = ValueInConstraint::class, validatedBy = [ValueInValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ValueIn(
    val values: Array<String>,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])