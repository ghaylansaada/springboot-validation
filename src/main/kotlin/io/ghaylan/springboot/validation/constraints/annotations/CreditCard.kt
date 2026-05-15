package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.creditcard.CreditCardConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.creditcard.CreditCardValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value contains only digits and passes the Luhn algorithm checksum.
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = CreditCardConstraint::class, validatedBy = [CreditCardValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class CreditCard(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)