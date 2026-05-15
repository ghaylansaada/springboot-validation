package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan.LessThanConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan.LessThanValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is less than (or equal to, if [inclusive]) a sibling field.
 * Both fields must be of the same `Comparable` type. Repeatable for multiple sibling comparisons.
 *
 * @property property Name of the sibling field to compare against.
 * @property inclusive When `true`, allows equality (`<=`); default is strict `<`.
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = LessThanConstraint::class, validatedBy = [LessThanValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class LessThan(
	val property: String,
	val inclusive: Boolean = false,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)