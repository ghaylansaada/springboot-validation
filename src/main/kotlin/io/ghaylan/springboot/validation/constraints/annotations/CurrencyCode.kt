package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.currency.CurrencyCodeConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.currency.CurrencyCodeValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated value represents a valid ISO 4217 currency code
 * (or each element in a collection/array is valid).
 *
 * ### Description
 * The `@CurrencyCode` annotation ensures that the annotated value (or each element in a collection)
 * matches a recognized three-letter currency code as defined by the [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217) standard.
 * This includes widely used codes like `USD` (US Dollar), `EUR` (Euro), or `JPY` (Japanese Yen),
 * as well as less common currencies recognized by the Java `Currency` API.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - A valid currency code must:
 *      - Consist of exactly **three uppercase letters**.
 *      - Exist in the list returned by `Currency.getAvailableCurrencies()`.
 * - For arrays or collections, each element must individually match a valid ISO 4217 currency code.
 *
 * ### Example Usage
 * ```kotlin
 * @CurrencyCode
 * val primaryCurrency: String
 * // ✅ Must be a valid ISO 4217 currency code (e.g., "USD", "EUR", "JPY")
 *
 * -----
 *
 * @CurrencyCode
 * val acceptedCurrencies: List<String>
 * // ✅ Each value must be a valid ISO 4217 currency code
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.ISO_CURRENCY_CODE_VIOLATION]
 *   Triggered when the value does not match a valid ISO 4217 currency code.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = CurrencyCodeConstraint::class, validatedBy = [CurrencyCodeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class CurrencyCode(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])