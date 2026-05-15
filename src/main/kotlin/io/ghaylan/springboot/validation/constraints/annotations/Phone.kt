package io.ghaylan.springboot.validation.constraints.annotations

import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.phone.PhoneConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.phone.PhoneValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a valid international phone number using Google's libphonenumber.
 * Optionally restricts accepted number types and countries.
 *
 * @property allowedTypes Permitted phone number types (e.g., MOBILE, FIXED_LINE); empty means any type.
 * @property allowedCountries Permitted ISO 3166-1 alpha-2 country codes; empty means any country.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = PhoneConstraint::class, validatedBy = [PhoneValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Phone(
	val allowedTypes: Array<PhoneNumberUtil.PhoneNumberType> = [],
	val allowedCountries: Array<String> = [],
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)