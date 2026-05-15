package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.equal.EqualToConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.equal.EqualToValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value equals the value of a sibling field in the same object
 * (e.g., password confirmation). Repeatable to compare against multiple siblings.
 *
 * @property property Name of the sibling field to compare against.
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = EqualToConstraint::class, validatedBy = [EqualToValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class EqualTo(
	val property: String,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)