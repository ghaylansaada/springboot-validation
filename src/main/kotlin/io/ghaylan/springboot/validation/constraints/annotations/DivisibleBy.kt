package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.number.divisible.DivisibleByConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.divisible.DivisibleByValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated numeric value is divisible by a given divisor without leaving a remainder.
 *
 * ### Description
 * The `@DivisibleBy` annotation ensures that the annotated value (or each element in a collection) is divisible
 * by the specified `divisor`. This is useful for enforcing numeric constraints such as step sizes,
 * price multiples, or business rules like "amount must be divisible by 0.05".
 *
 * ### Supported Types
 * - `Byte`
 * - `Short`
 * - `Int`
 * - `Long`
 * - `Float`
 * - `Double`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation is skipped if the divisor (`value`) is set to `0.0`.
 * - Values are compared with precision up to 10 decimal places.
 * - For collection/array types, each element must be divisible by the specified divisor.
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, allowing multiple constraints with different divisors to be applied on the same field/parameter.
 *
 * ### Example Usage
 * ```kotlin
 * @DivisibleBy(5.0)
 * val quantity: Int
 * // ✅ quantity must be divisible by 5.0
 *
 * -----
 *
 * @DivisibleBy(2.5)
 * val prices: List<Double>
 * // ✅ each price in prices must be divisible by 2.5
 *
 * -----
 *
 * @DivisibleBy(2.0)
 * @DivisibleBy(5.0)
 * val multiConstraintAmount: Double
 * // ✅ multiConstraintAmount must be divisible by both 2.0 and 5.0
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.DIVISIBILITY_VIOLATION]
 *   Triggered when a value is not divisible by the specified divisor.
 *
 * @property divisor The divisor to use for validation (must not be zero).
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = DivisibleByConstraint::class, validatedBy = [DivisibleByValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class DivisibleBy(
    val divisor: Double,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])