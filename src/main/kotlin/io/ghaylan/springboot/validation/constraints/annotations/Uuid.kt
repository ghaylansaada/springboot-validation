package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.uuid.UuidConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.uuid.UuidValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a valid UUID string (parseable by [java.util.UUID.fromString]).
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = UuidConstraint::class, validatedBy = [UuidValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Uuid(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)