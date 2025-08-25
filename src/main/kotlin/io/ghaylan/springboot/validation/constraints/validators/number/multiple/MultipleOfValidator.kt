package io.ghaylan.springboot.validation.constraints.validators.number.multiple

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlin.math.abs


object MultipleOfValidator : ConstraintValidator<Number, MultipleOfConstraint>()
{

    override suspend fun validate(
        value: Number?,
        constraint: MultipleOfConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        val number = value.toDouble()
        val multiplicator = constraint.factor

        val remainder = number % multiplicator
        val epsilon = 1e-9
        val isMultiple = remainder < epsilon || abs(remainder - multiplicator) < epsilon

        if (isMultiple) return null

        return ApiError(code = ApiErrorCode.MULTIPLICITY_VIOLATION, message = "Must be a multiple of `$multiplicator`")
    }
}