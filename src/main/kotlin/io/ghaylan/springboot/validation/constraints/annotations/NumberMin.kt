package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.number.min.NumberMinConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.min.NumberMinValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated numeric value is at least [value].
 *
 * @property value The minimum bound.
 * @property inclusive When `true` (default), allows equality (`>=`); when `false`, enforces strict `>`.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = NumberMinConstraint::class, validatedBy = [NumberMinValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NumberMin(
	val value: Double,
	val inclusive: Boolean = true,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)