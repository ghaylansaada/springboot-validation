package io.ghaylan.springboot.validation.constraints.validators.string.creditcard

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object CreditCardValidator : ConstraintValidator<CharSequence, CreditCardConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: CreditCardConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        if (isValid(value)) return null

        return ApiError(code = ApiErrorCode.CREDIT_CARD_PATTERN_VIOLATION, message = "Must be a valid Credit Card Number")
    }


    /**
     * Checks if a text value is a valid credit card number.
     *
     * Validates that the [value] contains only digits and passes the Luhn algorithm checksum.
     *
     * @param value The text value to validate (type [String]?).
     * @return `true` if the value is a valid credit card number, `false` otherwise.
     */
    fun isValid(value : CharSequence?) : Boolean
    {
        if (value.isNullOrBlank() || !value.all { it.isDigit() }) return false

        val digits = value.map { it.digitToInt() }

        val checksum = digits.reversed()
            .mapIndexed { index, digit ->
                if (index % 2 == 1) {
                    val doubled = digit * 2
                    if (doubled > 9) doubled - 9 else doubled
                } else digit
            }
            .sum()

        return checksum % 10 == 0
    }
}