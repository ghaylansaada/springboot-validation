package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuein.ValueInConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuein.ValueInValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value (or every element in a collection) is among the allowed [values]
 * after string conversion. Useful for restricting inputs to a fixed domain (e.g., roles, statuses).
 *
 * @property values The allowed string representations.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = ValueInConstraint::class, validatedBy = [ValueInValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ValueIn(
	val values: Array<String>,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)