package io.ghaylan.springboot.validation.constraints.validators.string.currency

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.util.Currency


object CurrencyCodeValidator : ConstraintValidator<CharSequence, CurrencyCodeConstraint>()
{
    private val currencies by lazy { fetchCurrencies() }


    override suspend fun validate(
        value: CharSequence?,
        constraint: CurrencyCodeConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        val currency = value?.toString() ?: return null

        if (currencies.contains(currency)) return null

        return ApiError(code = ApiErrorCode.ISO_CURRENCY_CODE_VIOLATION, message = "Must be a valid ISO 4217 currency code")
    }


    private fun fetchCurrencies() : List<String>
    {
        return Currency.getAvailableCurrencies()
            .map { it.currencyCode }
            .distinct()
            .sorted()
    }
}