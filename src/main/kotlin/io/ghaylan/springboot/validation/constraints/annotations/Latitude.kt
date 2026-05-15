package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.number.latitude.LatitudeConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.latitude.LatitudeValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated `Double` is a valid latitude coordinate: `[-90.0, 90.0]`.
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = LatitudeConstraint::class, validatedBy = [LatitudeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Latitude(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)