package io.ghaylan.springboot.validation.constraints.validators.required

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.constraints.annotations.Required.RequirementCondition
import io.ghaylan.springboot.validation.ext.isDeepNullOrEmpty
import io.ghaylan.springboot.validation.model.errors.ApiError


object RequiredValidator : ConstraintValidator<Any, RequiredConstraint>()
{

    override suspend fun validate(
        value: Any?,
        constraint: RequiredConstraint,
        context: ValidationContext
    ): ApiError?
    {
        if (!value.isDeepNullOrEmpty()) return null

        if (constraint.condition == RequirementCondition.ALWAYS || constraint.dependentField.isBlank())
        {
            return ApiError(code = ApiErrorCode.REQUIRED_VIOLATION, message = "Required")
        }

        val dependentField = super.getPropertyValue(name = constraint.dependentField, context = context)

        if (constraint.condition == RequirementCondition.IF_DEPENDENT_NULL && dependentField == null)
        {
            return ApiError(code = ApiErrorCode.REQUIRED_VIOLATION, message = "Required because the related field '${constraint.dependentField}' is missing")
        }

        if (constraint.condition == RequirementCondition.IF_DEPENDENT_NOT_NULL && dependentField != null)
        {
            return ApiError(code = ApiErrorCode.REQUIRED_VIOLATION, message = "Required because the related field '${constraint.dependentField}' is provided")
        }

        return null
    }
}