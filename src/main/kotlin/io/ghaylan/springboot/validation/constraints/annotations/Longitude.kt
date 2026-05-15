package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.number.longitude.LongitudeConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.longitude.LongitudeValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated `Double` is a valid longitude coordinate: `[-180.0, 180.0]`.
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = LongitudeConstraint::class, validatedBy = [LongitudeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Longitude(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)