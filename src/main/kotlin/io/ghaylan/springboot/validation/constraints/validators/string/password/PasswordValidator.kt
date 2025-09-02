package io.ghaylan.springboot.validation.constraints.validators.string.password

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlin.math.log2
import kotlin.text.iterator


object PasswordValidator : ConstraintValidator<CharSequence, PasswordConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: PasswordConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        return when
        {
            value.length !in constraint.minLength..constraint.maxLength ->
            {
                ApiErrorCode.PASSWORD_LENGTH_VIOLATION to "Must be between ${constraint.minLength} and ${constraint.maxLength} characters"
            }
            constraint.requireUppercase && value.none(Char::isUpperCase) ->
            {
                ApiErrorCode.PASSWORD_UPPERCASE_VIOLATION to "Must include at least one uppercase letter"
            }
            constraint.requireLowercase && value.none(Char::isLowerCase) ->
            {
                ApiErrorCode.PASSWORD_LOWERCASE_VIOLATION to "Must include at least one lowercase letter"
            }
            constraint.requireDigit && value.none(Char::isDigit) ->
            {
                ApiErrorCode.PASSWORD_DIGIT_VIOLATION to "Must include at least one digit"
            }
            constraint.requireSpecialChar && value.none { constraint.allowedSpecialChars.contains(it) } ->
            {
                ApiErrorCode.PASSWORD_SPECIAL_CHAR_VIOLATION to "Must include at least one of the following special character: ${constraint.allowedSpecialChars}"
            }
            constraint.minEntropy.entropy > calculateEntropy(value) ->
            {
                val strengthName = constraint.minEntropy.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

                ApiErrorCode.PASSWORD_ENTROPY_VIOLATION to "Must have at least ${constraint.minEntropy.entropy} bits of entropy (minimum required strength: $strengthName)"
            }
            else -> null

        }?.let { ApiError(code = it.first, message = it.second) }
    }


    /**
     * Calculates the entropy of a text value for password validation.
     *
     * Estimates the entropy based on the character set size (lowercase, uppercase, digits, special
     * characters) and the length of the [value]. Returns 0.0 for empty strings.
     *
     * @param value The text value to analyze (type [String]).
     * @return The calculated entropy in bits (type [Double]).
     */
    private fun calculateEntropy(value : CharSequence) : Double
    {
        if (value.isEmpty()) return 0.0

        var charsetSize = 0
        var hasLower = false
        var hasUpper = false
        var hasDigit = false
        var hasSpecial = false

        for (char in value)
        {
            when
            {
                char.isLowerCase() -> hasLower = true
                char.isUpperCase() -> hasUpper = true
                char.isDigit() -> hasDigit = true
                else -> hasSpecial = true
            }

            // Exit early if all character types are found
            if (hasLower && hasUpper && hasDigit && hasSpecial) break
        }

        // Estimate charset size based on detected character types
        if (hasLower) charsetSize += 26
        if (hasUpper) charsetSize += 26
        if (hasDigit) charsetSize += 10
        if (hasSpecial) charsetSize += 32 // Common printable symbols

        return if (charsetSize == 0) 0.0 else value.length * log2(charsetSize.toDouble())
    }
}