package io.ghaylan.springboot.validation.constraints.validators.string.phone

import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass


/** Constraint metadata for [@Phone][io.ghaylan.springboot.validation.constraints.annotations.Phone]. */
data class PhoneConstraint(
	val allowedTypes: Set<PhoneNumberType>,
	val allowedCountries: Set<String>,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()