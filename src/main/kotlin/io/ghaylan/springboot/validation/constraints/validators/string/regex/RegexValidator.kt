package io.ghaylan.springboot.validation.constraints.validators.string.regex

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.regex.Pattern


object RegexValidator : ConstraintValidator<CharSequence, RegexConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: RegexConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        val pattern = Pattern.compile(constraint.pattern)

        if (pattern.matcher(value).matches()) return null

        return ApiError(code = ApiErrorCode.PATTERN_VIOLATION, message = "Must match the required expression")
    }
}