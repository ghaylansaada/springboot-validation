package io.ghaylan.springboot.validation.constraints.validators.string.contains

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.constraints.annotations.StrOcc.StrOccMode
import io.ghaylan.springboot.validation.model.errors.ApiError


object StrOccValidator : ConstraintValidator<CharSequence, StrOccConstraint>()
{
    override suspend fun validate(
        value: CharSequence?,
        constraint: StrOccConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val errorMessage = StringBuilder()

        when (constraint.mode)
        {
            StrOccMode.EQUALS if !value.contentEquals(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must be equal to ${constraint.value}")
            }
            StrOccMode.CONTAINS if !value.contains(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must contain ${constraint.value}")
            }
            StrOccMode.STARTS_WITH if !value.startsWith(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must start with ${constraint.value}")
            }
            StrOccMode.ENDS_WITH if !value.endsWith(constraint.value, constraint.ignoreCase) -> {
                errorMessage.append("Must end with ${constraint.value}")
            }
            else -> return null
        }

        val occurrences = countOccurrences(
            text = value.toString(),
            sub = constraint.value,
            ignoreCase = constraint.ignoreCase)

        if (occurrences !in constraint.minOccurrences..constraint.maxOccurrences)
        {
            if (errorMessage.isEmpty())
            {
                errorMessage.append("Must have between ${constraint.minOccurrences} and ${constraint.maxOccurrences} occurrences of `${constraint.value}`")
            }
            else errorMessage.append(", with between ${constraint.minOccurrences} and ${constraint.maxOccurrences} occurrences of `${constraint.value}`")
        }

        if (errorMessage.isNotEmpty())
        {
            errorMessage.append(if (constraint.ignoreCase) " (ignoring case)" else "")

            return ApiError(code = ApiErrorCode.STRING_OCCURRENCES_VIOLATION, message = errorMessage.toString())
        }

        return null
    }


    private fun countOccurrences(
        text: String,
        sub: String,
        ignoreCase : Boolean
    ): Int
    {
        if (sub.isEmpty()) return 0

        var count = 0
        var index = 0

        while (true)
        {
            index = text.indexOf(sub, index, ignoreCase)
            if (index < 0) break
            count++
            index += sub.length
        }

        return count
    }
}