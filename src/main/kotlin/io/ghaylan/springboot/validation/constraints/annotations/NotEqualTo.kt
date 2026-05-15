package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.notequal.NotEqualToConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.notequal.NotEqualToValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value does not equal the value of a sibling field in the same object.
 * Repeatable to compare against multiple siblings independently.
 *
 * @property property Name of the sibling field to compare against.
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = NotEqualToConstraint::class, validatedBy = [NotEqualToValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NotEqualTo(
	val property: String,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)