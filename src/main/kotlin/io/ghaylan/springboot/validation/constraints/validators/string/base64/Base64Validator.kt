package io.ghaylan.springboot.validation.constraints.validators.string.base64

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.Base64


object Base64Validator : ConstraintValidator<CharSequence, Base64Constraint>()
{
    private val regex by lazy { Regex("^[A-Za-z0-9+/]*={0,2}$") }


    override suspend fun validate(
        value: CharSequence?,
        constraint: Base64Constraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        if (isValidBase64(value.toString())) return null

        return ApiError(code = ApiErrorCode.BASE64_VIOLATION, message = "Must be a valid Base64 encoded string")
    }


    fun isValidBase64(input: String): Boolean
    {
        // Basic regex check to ensure it only contains valid Base64 characters
        if (!input.matches(regex)) return false

        // Check length is a multiple of 4
        if (input.length % 4 != 0) return false

        return runCatching {
            Base64.getDecoder().decode(input)
        }.getOrNull() != null
    }
}