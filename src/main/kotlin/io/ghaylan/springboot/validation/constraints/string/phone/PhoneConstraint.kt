package io.ghaylan.springboot.validation.constraints.string.phone

import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import kotlin.reflect.KClass


data class PhoneConstraint(
    val allowedTypes: Set<PhoneNumberType>,
    val allowedCountries: Set<String>,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()