package io.ghaylan.springboot.validation.constraints.validators.number.longitude

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object LongitudeValidator : ConstraintValidator<Double, LongitudeConstraint>()
{

    override suspend fun validate(
        value: Double?,
        constraint: LongitudeConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        if (isValid(value)) return null

        return ApiError(code = ApiErrorCode.LONGITUDE_VIOLATION, message = "Must be a valid longitude between -180.0 and 180.0 degrees, inclusive")
    }


    /**
     * Checks if a decimal value is a valid longitude.
     *
     * A valid longitude is a [Double] value between -180.0 and 180.0 degrees, inclusive.
     *
     * @param longitude The decimal value to check (type [Double]).
     * @return `true` if the value is a valid longitude, `false` otherwise.
     */
    fun isValid(longitude : Double) : Boolean
    {
        return longitude in -180.0..180.0
    }
}