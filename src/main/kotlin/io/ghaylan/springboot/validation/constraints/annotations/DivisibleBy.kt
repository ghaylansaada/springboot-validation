package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.number.divisible.DivisibleByConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.divisible.DivisibleByValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated numeric value (or each element in a collection) is divisible by [divisor]
 * without a remainder. Repeatable to combine multiple divisibility rules on the same field.
 *
 * @property divisor The divisor; a value of `0.0` skips validation.
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = DivisibleByConstraint::class, validatedBy = [DivisibleByValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class DivisibleBy(
	val divisor: Double,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)