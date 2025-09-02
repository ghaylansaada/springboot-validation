package io.ghaylan.springboot.validation.constraints.validators.string.language

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.Locale


object LanguageValidator : ConstraintValidator<CharSequence, LanguageConstraint>()
{
    private val allLanguages by lazy { fetchAllLanguages() }


    override suspend fun validate(
        value: CharSequence?,
        constraint: LanguageConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        val valueAsString = value.toString()

        if (allLanguages.contains(valueAsString)) return null

        return ApiError(code = ApiErrorCode.ISO_LANGUAGE_CODE_VIOLATION, message =
                "Must be a valid language tag in the format 'xx' or 'xx-YY', " +
                "where 'xx' is a 2-letter ISO 639-1 language code and 'YY' is a 2-letter ISO 3166-1 country code.")
    }


    private fun fetchAllLanguages() : List<String>
    {
        val regex = Regex("^[a-z]{2}(-[A-Z]{2})?$") // match only "xx" or "xx-YY"

        return Locale.getAvailableLocales()
            .map { it.toLanguageTag() }
            .filter { regex.matches(it) }
            .distinct()
            .sorted()
    }
}