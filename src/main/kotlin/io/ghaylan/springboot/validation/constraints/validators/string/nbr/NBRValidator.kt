package io.ghaylan.springboot.validation.constraints.validators.string.nbr

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.regex.Pattern


object NBRValidator : ConstraintValidator<CharSequence, NBRConstraint>()
{
    private val PATTERN = Pattern.compile("[0-9]{7}[ABCDEFGHJKLMNPQRSTVWXYZ]")


    override suspend fun validate(
        value: CharSequence?,
        constraint: NBRConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (isValid(value)) return null

        return ApiError(code = ApiErrorCode.NBR_FORMAT_VIOLATION, message = "Must be a valid National Business Registry number")
    }


    /**
     * Checks if a text value is a valid National Business Register (NBR) number.
     *
     * Validates that the [value] is a 2-8 character NBR, padded to 8 characters, with a valid
     * checksum letter based on a weighted sum modulo 23.
     *
     * @param value The text value to validate (type [String]).
     * @return `true` if the value is a valid NBR, `false` otherwise.
     */
    fun isValid(value : CharSequence) : Boolean
    {
        if (value.length !in 2..8) return false

        // Pad and capitalize last character for validation
        val finalValue = value.padStart(8, '0').capitalizeLastChar()

        if (!PATTERN.matcher(finalValue).matches()) return false

        val index = finalValue.take(7)
            .mapIndexed { index, char ->
                char.digitToInt() * (7 - index)
            }
            .sum()
            .mod(23)

        return "ABCDEFGHJKLMNPQRSTVWXYZ".getOrNull(index) == finalValue.last()
    }


    private fun CharSequence.capitalizeLastChar() : CharSequence
    {
        if (isEmpty()) return this

        val lastIndex = lastIndex

        val lastChar = this[lastIndex]

        return if (lastChar.isLetter())
        {
            replaceRange(lastIndex, lastIndex + 1, lastChar.uppercaseChar().toString())
        }
        else this
    }
}