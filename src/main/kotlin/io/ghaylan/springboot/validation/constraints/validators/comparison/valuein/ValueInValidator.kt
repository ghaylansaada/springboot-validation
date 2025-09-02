package io.ghaylan.springboot.validation.constraints.validators.comparison.valuein

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.utils.ReflectionUtils


object ValueInValidator : ConstraintValidator<Any, ValueInConstraint>()
{

    override suspend fun validate(
        value: Any?,
        constraint: ValueInConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (!ReflectionUtils.isScalar(value)) return null

        val asString = value.toString()

        if (asString !in constraint.values) return null

        return ApiError(code = ApiErrorCode.ALLOWED_VALUE_VIOLATION, message = "Must be one of the following values: ${constraint.values}")
    }
}