package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.number.max.NumberMaxConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.max.NumberMaxValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated numeric value does not exceed [value].
 *
 * @property value The maximum bound.
 * @property inclusive When `true` (default), allows equality (`<=`); when `false`, enforces strict `<`.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = NumberMaxConstraint::class, validatedBy = [NumberMaxValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NumberMax(
	val value: Double,
	val inclusive: Boolean = true,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)