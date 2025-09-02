package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.comparison.equal.EqualToConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.equal.EqualToValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is equal to the value of another field/parameter in the same object.
 *
 * ### Description
 * The `@EqualTo` annotation ensures that the annotated value is equal to the value of another **sibling** property
 * within the same object. This is particularly useful in scenarios like password confirmation, email re-entry, or
 * any case where two fields must carry the same value.
 *
 * ### Supported Types
 * - Any type implementing `Comparable<T>`, including but not limited to:
 *      - `String`, `Int`, `Double`, `BigDecimal`, `LocalDate`, `LocalDateTime`
 *      - Any user-defined class implementing `Comparable<T>`
 *
 * ### Validation Rules
 * - The `property` must point to a **sibling field/parameter** within the same object.
 * - Both the annotated field/parameter and the referenced field/parameter must:
 *      - Be non-null at the time of validation
 *      - Have exactly the same Kotlin type (checked via `::class`)
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, allowing multiple comparisons from the same field to multiple sibling fields/parameters.
 *
 * ### Example Usage
 * ```kotlin
 * @EqualTo(property = "password")
 * val confirmPassword: String)
 * // ✅ Ensures confirmPassword is equal to password
 *
 * -----
 *
 * @EqualTo(property = "a")
 * @EqualTo(property = "b")
 * val c: String
 * // ✅ Ensures c equals both a and b
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.EQUALITY_VIOLATION]
 *   Triggered when the annotated value does not match the value of the sibling field/parameter.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.DEPENDENCY_TYPE_VIOLATION]
 *   Triggered when the two fields/parameters are not of the same type and cannot be compared.
 *
 * @property property Name of the sibling field/parameter to compare the annotated value to (must be in the same object).
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = EqualToConstraint::class, validatedBy = [EqualToValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class EqualTo(
    val property: String,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])