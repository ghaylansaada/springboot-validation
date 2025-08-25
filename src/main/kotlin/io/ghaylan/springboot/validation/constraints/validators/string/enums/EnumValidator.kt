package io.ghaylan.springboot.validation.constraints.validators.string.enums

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode

object EnumValidator : ConstraintValidator<CharSequence, EnumConstraint>()
{
    private val regex by lazy { Regex("""^[A-Z][A-Z0-9_]*$""") }


    override suspend fun validate(
        value: CharSequence?,
        constraint: EnumConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (isEnumStyle(value = value.toString(), ignoreCase = constraint.ignoreCase)) return null

        return ApiError(code = ApiErrorCode.ENUM_FORMAT_VIOLATION, message = "Must be a valid enum style")
    }


    fun isEnumStyle(value: String, ignoreCase: Boolean = false): Boolean
    {
        return if (ignoreCase)
        {
            value.uppercase().matches(regex)
        }
        else value.matches(regex)
    }
}