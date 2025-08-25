package io.ghaylan.springboot.validation.constraints.validators.string.country

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.Locale

object ISOCountryValidator : ConstraintValidator<CharSequence, ISOCountryConstraint>()
{
    private val countries by lazy { Locale.getISOCountries() }


    override suspend fun validate(
        value: CharSequence?,
        constraint: ISOCountryConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        val country = value?.toString() ?: return null

        if (countries.contains(country)) return null

        return ApiError(code = ApiErrorCode.ISO_COUNTRY_CODE_VIOLATION, message = "Must be a valid ISO 3166-1 alpha-2 code (e.g., 'US', 'FR', 'JP')")
    }
}