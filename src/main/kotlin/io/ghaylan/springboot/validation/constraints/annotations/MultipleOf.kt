package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.number.multiple.MultipleOfConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.multiple.MultipleOfValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated numeric value (or each element in a collection) is a multiple of [factor].
 * Repeatable to combine multiple multiplicity rules on the same field.
 *
 * @property factor The factor; a value of `0.0` skips validation.
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = MultipleOfConstraint::class, validatedBy = [MultipleOfValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class MultipleOf(
	val factor: Double,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)