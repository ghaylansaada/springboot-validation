package io.ghaylan.springboot.validation.constraints.validators.string.iban

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object IBANValidator : ConstraintValidator<CharSequence, IBANConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: IBANConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        if (isValid(value.toString())) return null

        return ApiError(code = ApiErrorCode.IBAN_FORMAT_VIOLATION, message = "Invalid International Bank Account Number Format")
    }


    /**
     * Checks if a text value is a valid IBAN.
     *
     * Validates that the [value] is a 20-character International Bank Account Number with a valid
     * checksum based on modulo 97 arithmetic.
     *
     * @param value The text value to validate (type [String]?).
     * @return `true` if the value is a valid IBAN, `false` otherwise.
     */
    fun isValid(value : String?) : Boolean
    {
        if (value.isNullOrBlank()) return false

        return if (value.length == 20)
        {
            val firstDigits : String = value.take(9)
            val lastDigits : String = value.substring(value.length - 11 , value.length)

            val a : Long = (firstDigits.toLongOrNull() ?: return false) % 97
            val b : String = a.toString().plus(lastDigits)

            b.toLong() % 97 == 0L
        }
        else false
    }
}