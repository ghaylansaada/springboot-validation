package io.ghaylan.springboot.validation.constraints.validators.temporal.max

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.ext.isBefore
import io.ghaylan.springboot.validation.ext.isEqual
import io.ghaylan.springboot.validation.ext.toTemporal
import io.ghaylan.springboot.validation.model.errors.ApiError
import java.time.temporal.Temporal


object TemporalMaxValidator : ConstraintValidator<Temporal, TemporalMaxConstraint>()
{

    override suspend fun validate(
        value: Temporal?,
        constraint: TemporalMaxConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val maxValue = constraint.value.toTemporal(value::class)

        if (value.isBefore(maxValue) || (value.isEqual(maxValue) && constraint.inclusive)) return null

        val comparison = if (constraint.inclusive) "before or equal to" else "strictly before"

        return ApiError(code = ApiErrorCode.MAX_VALUE_VIOLATION, message = "Must be $comparison to `${constraint.value}`")
    }
}