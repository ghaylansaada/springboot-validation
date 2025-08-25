package io.ghaylan.springboot.validation.constraints.validators.string.email

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.regex.Pattern


object EmailValidator : ConstraintValidator<CharSequence, EmailConstraint>()
{
    private val PATTERN = Pattern.compile("^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$")


    override suspend fun validate(
        value: CharSequence?,
        constraint: EmailConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        if (isValid(value)) return null

        return ApiError(code = ApiErrorCode.EMAIL_FORMAT_VIOLATION, message = "Must be a valid email address")
    }


    fun isValid(value : CharSequence) : Boolean
    {
        return PATTERN.matcher(value).matches()
    }
}