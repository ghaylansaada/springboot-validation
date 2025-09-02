package io.ghaylan.springboot.validation.constraints.validators.number.max

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object NumberMaxValidator : ConstraintValidator<Number, NumberMaxConstraint>()
{

    override suspend fun validate(
        value: Number?,
        constraint: NumberMaxConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val asDouble = value.toDouble()

        if (asDouble < constraint.value || (asDouble == constraint.value && constraint.inclusive)) return null

        val comparison = if (constraint.inclusive) "less than or equal to" else "strictly less than"

        return ApiError(code = ApiErrorCode.MAX_VALUE_VIOLATION, message = "Must be $comparison ${constraint.value}")
    }
}