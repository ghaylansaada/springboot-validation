package io.ghaylan.springboot.validation.constraints.validators.string.notcontains

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.constraints.annotations.StrOcc.StrOccMode
import io.ghaylan.springboot.validation.model.errors.ApiError


object NotStrOccValidator : ConstraintValidator<CharSequence, NotStrOccConstraint>()
{
    override suspend fun validate(
        value: CharSequence?,
        constraint: NotStrOccConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        val errorMessage = StringBuilder()

        when (constraint.mode)
        {
            StrOccMode.EQUALS if value.contentEquals(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must not be equal to ${constraint.value}")
            }
            StrOccMode.CONTAINS if value.contains(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must not contain ${constraint.value}")
            }
            StrOccMode.STARTS_WITH if value.startsWith(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must not start with ${constraint.value}")
            }
            StrOccMode.ENDS_WITH if value.endsWith(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must not end with ${constraint.value}")
            }
            else -> return null
        }

        if (errorMessage.isNotEmpty())
        {
            errorMessage.append(if (constraint.ignoreCase) " (ignoring case)" else "")

            return ApiError(code = ApiErrorCode.STRING_OCCURRENCES_VIOLATION, message = errorMessage.toString())
        }

        return null
    }
}