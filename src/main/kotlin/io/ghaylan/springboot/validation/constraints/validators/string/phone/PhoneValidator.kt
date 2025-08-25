package io.ghaylan.springboot.validation.constraints.validators.string.phone

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.utils.PhoneNumberUtils


object PhoneValidator : ConstraintValidator<CharSequence, PhoneConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: PhoneConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (!PhoneNumberUtils.isValidNumber(value))
        {
            return ApiError(code = ApiErrorCode.PHONE_FORMAT_VIOLATION, message = "Must be a valid international phone number")
        }

        // Check allowed number types (e.g., mobile, landline)
        if (constraint.allowedTypes.isNotEmpty() && !constraint.allowedTypes.contains(PhoneNumberUtils.getNumberType(value)))
        {
            val types = constraint.allowedTypes.joinToString(separator = ", ") { it.name.lowercase() }

            return ApiError(code = ApiErrorCode.PHONE_TYPE_VIOLATION, message = "Must be one of the following allowed phone number types: $types")
        }

        // Check allowed country codes
        if (constraint.allowedCountries.isNotEmpty() && !constraint.allowedCountries.contains(PhoneNumberUtils.getCountryISOCode(value)))
        {
            val countries = constraint.allowedCountries.joinToString(separator = ", ") { it.uppercase() }

            return ApiError(code = ApiErrorCode.PHONE_COUNTRY_VIOLATION, message = "Must be a phone number from one of the following allowed countries: $countries")
        }

        return null
    }
}