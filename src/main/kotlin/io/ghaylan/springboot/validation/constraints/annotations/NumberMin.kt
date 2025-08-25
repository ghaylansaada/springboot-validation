package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.number.min.NumberMinConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.min.NumberMinValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated numeric value is greater than (or equal to) a specified minimum.
 *
 * ### Description
 * The `@NumberMin` annotation ensures that the annotated value (or each element in a collection) is greater than
 * or equal to the specified minimum. It can be configured to be inclusive or exclusive, depending on the `inclusive` flag.
 * This is useful for enforcing lower bounds on inputs such as age, price, quantity, and thresholds.
 *
 * ### Supported Types
 * - Any type extending `Number`
 * - Common built-in types: `Byte`, `Short`, `Int`, `Long`, `Float`, `Double`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - The value must be greater than the configured `value`.
 * - If `inclusive = true`, the value is allowed to equal the configured minimum.
 * - If `inclusive = false`, the value must be strictly greater than the minimum.
 * - For collection/array types, each element is validated individually.
 *
 * ### Example Usage
 * ```kotlin
 * @NumberMin(value = 1.0)
 * val quantity: Int
 * // ✅ quantity must be ≥ 1
 *
 * -----
 *
 * @NumberMin(value = 0.01, inclusive = false)
 * val price: Double
 * // ✅ price must be > 0.01
 *
 * -----
 *
 * @NumberMin(value = 100.0)
 * val discounts: List<Double>
 * // ✅ each discount in discounts must be ≥ 100.0
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.MIN_VALUE_VIOLATION]
 *   Triggered when a value is below the configured minimum.
 *
 * @property value The minimum value allowed.
 * @property inclusive Whether the comparison is inclusive (default is `true`).
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = NumberMinConstraint::class, validatedBy = [NumberMinValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NumberMin(
    val value : Double,
    val inclusive : Boolean = true,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])