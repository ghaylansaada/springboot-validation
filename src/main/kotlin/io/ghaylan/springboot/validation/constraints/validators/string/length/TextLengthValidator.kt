package io.ghaylan.springboot.validation.constraints.validators.string.length

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object TextLengthValidator : ConstraintValidator<CharSequence, TextLengthConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: TextLengthConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (value.length in constraint.min..constraint.max) return null

        return ApiError(code = ApiErrorCode.STRING_LENGTH_VIOLATION, message = "Text must contain between ${constraint.min} and ${constraint.max} characters")
    }
}