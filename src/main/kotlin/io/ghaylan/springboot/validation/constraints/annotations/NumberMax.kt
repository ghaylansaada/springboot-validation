package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.number.max.NumberMaxConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.max.NumberMaxValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated numeric value is less than (or equal to) a specified maximum.
 *
 * ### Description
 * The `@NumberMax` annotation ensures that the annotated value (or each element in a collection) is less than
 * or equal to the specified maximum. It can be configured to be inclusive or exclusive, depending on the `inclusive` flag.
 * This is useful for enforcing upper bounds on inputs such as age, price, speed, and thresholds.
 *
 * ### Supported Types
 * - Any type extending `Number`
 * - Common built-in types: `Byte`, `Short`, `Int`, `Long`, `Float`, `Double`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - The value must be less than the configured `value`.
 * - If `inclusive = true`, the value is allowed to equal the configured maximum.
 * - If `inclusive = false`, the value must be strictly less than the maximum.
 * - For collection/array types, each element is validated individually.
 *
 * ### Example Usage
 * ```kotlin
 * @NumberMax(value = 120.0)
 * val age: Int
 * // ✅ age must be ≤ 120
 *
 * -----
 *
 * @NumberMax(value = 99.99, inclusive = false)
 * val speed: Double
 * // ✅ speed must be < 99.99
 *
 * -----
 *
 * @NumberMax(value = 1000.0)
 * val values: List<Long>
 * // ✅ each value in values must be ≤ 1000.0
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.MAX_VALUE_VIOLATION]
 *   Triggered when a value exceeds the configured maximum.
 *
 * @property value The maximum value allowed.
 * @property inclusive Whether the comparison is inclusive (default is `true`).
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = NumberMaxConstraint::class, validatedBy = [NumberMaxValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NumberMax(
    val value : Double,
    val inclusive : Boolean = true,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])