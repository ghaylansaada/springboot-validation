package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.currency.CurrencyCodeConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.currency.CurrencyCodeValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a recognized ISO 4217 three-letter currency code (e.g. "USD", "EUR").
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = CurrencyCodeConstraint::class, validatedBy = [CurrencyCodeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class CurrencyCode(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)