package io.ghaylan.springboot.validation.constraints.validators.string.hexcolor

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.regex.Pattern


object HexColorValidator : ConstraintValidator<CharSequence, HexColorConstraint>()
{
    private val PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")


    override suspend fun validate(
        value: CharSequence?,
        constraint: HexColorConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (isValid(value)) return null

        return ApiError(code = ApiErrorCode.HEX_COLOR_CODE_FORMAT_VIOLATION, message = "Must be a valid hexadecimal color code")
    }


    fun isValid(value : CharSequence) : Boolean
    {
        return PATTERN.matcher(value).matches()
    }
}