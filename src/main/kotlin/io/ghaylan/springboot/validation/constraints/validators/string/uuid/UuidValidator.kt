package io.ghaylan.springboot.validation.constraints.validators.string.uuid

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.UUID


object UuidValidator : ConstraintValidator<CharSequence, UuidConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: UuidConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val isValid = runCatching {
            UUID.fromString(value.toString())
        }.isSuccess

        if (isValid) return null

        return ApiError(code = ApiErrorCode.UUID_PATTERN_VIOLATION, message = "Must be a valid UUID string, e.g. 123e4567-e89b-12d3-a456-426614174000")
    }
}