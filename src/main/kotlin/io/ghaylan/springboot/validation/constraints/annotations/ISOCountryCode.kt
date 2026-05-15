package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.country.ISOCountryConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.country.ISOCountryValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a recognized ISO 3166-1 alpha-2 country code (e.g. "US", "FR").
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = ISOCountryConstraint::class, validatedBy = [ISOCountryValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ISOCountryCode(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)