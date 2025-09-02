package io.ghaylan.springboot.validation.constraints.validators.number.latitude

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object LatitudeValidator : ConstraintValidator<Double, LatitudeConstraint>()
{

    override suspend fun validate(
        value: Double?,
        constraint: LatitudeConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        if (isValid(value)) return null

        return ApiError(code = ApiErrorCode.LATITUDE_VIOLATION, message = "Must be a valid latitude between -90.0 and 90.0 degrees, inclusive")
    }


    /**
     * Checks if a decimal value is a valid latitude.
     *
     * A valid latitude is a [Double] value between -90.0 and 90.0 degrees, inclusive.
     *
     * @param latitude The decimal value to check (type [Double]).
     * @return `true` if the value is a valid latitude, `false` otherwise.
     */
    fun isValid(latitude : Double) : Boolean
    {
        return latitude in -90.0..90.0
    }
}