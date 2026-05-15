package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuenotin.ValueNotInConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuenotin.ValueNotInValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value (or every element in a collection) is NOT among the disallowed [values]
 * after string conversion. Useful for blocking forbidden inputs (e.g., banned statuses, deprecated codes).
 *
 * @property values The disallowed string representations.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = ValueNotInConstraint::class, validatedBy = [ValueNotInValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ValueNotIn(
	val values: Array<String>,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)