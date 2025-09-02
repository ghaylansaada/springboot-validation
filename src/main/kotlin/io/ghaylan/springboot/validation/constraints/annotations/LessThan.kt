package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan.LessThanConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan.LessThanValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is less than the value of another field/parameter in the same object.
 *
 * ### Description
 * The `@LessThan` annotation ensures that the annotated value is less than the value of another
 * **sibling** field/parameter within the same object. This is useful for enforcing relative order between
 * logically dependent fields/parameters, such as ensuring `startDate` is before `endDate`, or `min` is less than `max`.
 *
 * ### Supported Types
 * - Any type implementing `Comparable<T>`
 * - Common types include:
 *      - Numeric types: `Int`, `Long`, `Double`, `BigDecimal`, etc.
 *      - Temporal types: `LocalDate`, `LocalDateTime`, etc.
 *      - Lexicographical types: `String`
 *      - Custom types that implement `Comparable`
 *
 * ### Validation Rules
 * - The `property` parameter must refer to another field/parameter at the **same object level**.
 * - Both fields/parameters must be of the same type.
 * - If the annotated value is greater than the target, validation fails.
 * - If both are equal and `inclusive = false`, validation fails.
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, allowing multiple applications on the same field/parameter to compare
 * it against multiple other fields independently.
 *
 * ### Example Usage
 * ```kotlin
 * @LessThan(property = "endDate")
 * val startDate: LocalDate
 * // ✅ Ensures startDate is before endDate
 *
 * -----
 *
 * @LessThan(property = "max", inclusive = true)
 * val min: Int
 * // ✅ Ensures min is less than or equal to max
 *
 * -----
 *
 * @LessThan(property = "fieldB")
 * @LessThan(property = "fieldC")
 * val fieldA: String
 * // ✅ Ensures fieldA is less than both fieldB and fieldC (alphabetical comparison)
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.LESS_THAN_VIOLATION]
 *   Thrown when the annotated value is not less than (or not less than or equal to) the target value.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.DEPENDENCY_TYPE_VIOLATION]
 *   Thrown when the annotated field/parameter and target field/parameter are of different types.
 *
 * @property property Name of the other sibling field/parameter to compare against (must be in the same object level).
 * @property inclusive Whether equality is allowed (i.e. `<=`). Default is `false` for strict less-than comparison.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = LessThanConstraint::class, validatedBy = [LessThanValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class LessThan(
    val property: String,
    val inclusive: Boolean = false,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])