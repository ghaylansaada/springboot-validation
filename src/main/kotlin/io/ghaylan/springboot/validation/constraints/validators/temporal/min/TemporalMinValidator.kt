package io.ghaylan.springboot.validation.constraints.validators.temporal.min

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.ext.isAfter
import io.ghaylan.springboot.validation.ext.isEqual
import io.ghaylan.springboot.validation.ext.toTemporal
import io.ghaylan.springboot.validation.model.errors.ApiError
import java.time.temporal.Temporal


object TemporalMinValidator : ConstraintValidator<Temporal, TemporalMinConstraint>()
{

    override suspend fun validate(
        value: Temporal?,
        constraint: TemporalMinConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val minValue = constraint.value.toTemporal(value::class)

        if (value.isAfter(minValue) || (value.isEqual(minValue) && constraint.inclusive)) return null

        val comparison = if (constraint.inclusive) "after or equal to" else "strictly after"

        return ApiError(code = ApiErrorCode.MIN_VALUE_VIOLATION, message = "Must be $comparison to `${constraint.value}`")
    }
}