package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.iban.IBANConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.iban.IBANValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a structurally valid IBAN (20 characters, ISO 13616 modulo-97 checksum).
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = IBANConstraint::class, validatedBy = [IBANValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class IBAN(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)