package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.comparison.greaterthan.GreaterThanConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.greaterthan.GreaterThanValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is greater than the value of another field/parameter in the same object.
 *
 * ### Description
 * The `@GreaterThan` annotation ensures that the annotated value is greater than the value of another
 * **sibling** field/parameter within the same object. This is useful for enforcing relative order between
 * logically dependent fields/parameters, such as ensuring `endDate` is after `startDate`, or `max` is greater than `min`.
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
 * - If the annotated value is less than the target, validation fails.
 * - If both are equal and `inclusive = false`, validation fails.
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, allowing multiple applications on the same field/parameter to compare
 * it against multiple other fields independently.
 *
 * ### Example Usage
 * ```kotlin
 * @GreaterThan(property = "startDate")
 * val endDate: LocalDate
 * // ✅ Ensures endDate is after startDate
 *
 * -----
 *
 * @GreaterThan(property = "min", inclusive = true)
 * val max: Int
 * // ✅ Ensures max is greater than or equal to min
 *
 * -----
 *
 * @GreaterThan(property = "fieldB")
 * @GreaterThan(property = "fieldC")
 * val fieldA: String
 * // ✅ Ensures fieldA is greater than both fieldB and fieldC (alphabetical comparison)
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.GREATER_THAN_VIOLATION]
 *   Thrown when the annotated value is not greater than (or not greater than or equal to) the target value.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.DEPENDENCY_TYPE_VIOLATION]
 *   Thrown when the annotated field/parameter and target field/parameter are of different types.
 *
 * @property property Name of the other sibling field/parameter to compare against (must be in the same object level).
 * @property inclusive Whether equality is allowed (i.e. `>=`). Default is `false` for strict greater-than comparison.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = GreaterThanConstraint::class, validatedBy = [GreaterThanValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class GreaterThan(
    val property: String,
    val inclusive: Boolean = false,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])