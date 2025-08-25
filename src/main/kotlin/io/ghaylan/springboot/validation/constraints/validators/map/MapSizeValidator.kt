package io.ghaylan.springboot.validation.constraints.validators.map

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object MapSizeValidator : ConstraintValidator<Map<*,*>, MapSizeConstraint>()
{

    override suspend fun validate(
        value: Map<*, *>?,
        constraint: MapSizeConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (value.size in constraint.min..constraint.max) return null

        return ApiError(code = ApiErrorCode.OBJECT_SIZE_VIOLATION, message = "Object must contain between ${constraint.min} and ${constraint.max} key-value pairs")
    }
}