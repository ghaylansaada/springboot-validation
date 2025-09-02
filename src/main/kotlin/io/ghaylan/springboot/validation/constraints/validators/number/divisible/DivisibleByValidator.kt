package io.ghaylan.springboot.validation.constraints.validators.number.divisible

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlin.math.abs


object DivisibleByValidator : ConstraintValidator<Number, DivisibleByConstraint>()
{

    override suspend fun validate(
        value: Number?,
        constraint: DivisibleByConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val number = value.toDouble()

        val divisor = constraint.divisor

        val remainder = number % divisor
        val epsilon = 1e-9
        val isDivisible = remainder < epsilon || abs(remainder - divisor) < epsilon

        if (isDivisible) return null

        return ApiError(code = ApiErrorCode.DIVISIBILITY_VIOLATION, message = "Must be divisible by `$divisor`")
    }
}