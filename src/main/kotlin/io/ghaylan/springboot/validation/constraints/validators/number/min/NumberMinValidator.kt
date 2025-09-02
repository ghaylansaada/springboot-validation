package io.ghaylan.springboot.validation.constraints.validators.number.min

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object NumberMinValidator : ConstraintValidator<Number, NumberMinConstraint>()
{

    override suspend fun validate(
        value: Number?,
        constraint: NumberMinConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        val asDouble = value.toDouble()

        if (asDouble > constraint.value || (asDouble == constraint.value && constraint.inclusive)) return null

        val comparison = if (constraint.inclusive) "greater than or equal to" else "strictly greater than"

        return ApiError(code = ApiErrorCode.MIN_VALUE_VIOLATION, message = "Must be $comparison ${constraint.value}")
    }
}