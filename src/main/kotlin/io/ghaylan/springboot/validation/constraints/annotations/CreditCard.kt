package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.creditcard.CreditCardConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.creditcard.CreditCardValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence represents a valid credit card number.
 *
 * ### Description
 * The `@CreditCard` annotation ensures that the annotated value (or each element in a collection)
 * contains only numeric digits and passes the **Luhn algorithm** checksum validation.
 * This is commonly used for validating payment card numbers (e.g., Visa, MasterCard, American Express)
 * in APIs, form submissions, or data models.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The value must contain **only digits** (`0–9`).
 * - The value must satisfy the Luhn algorithm checksum.
 * - For arrays or collections, each element must individually meet the above rules.
 *
 * ### Example Usage
 * ```kotlin
 * @CreditCard
 * val cardNumber: String
 * // ✅ Must be digits only and pass the Luhn checksum
 *
 * -----
 *
 * @CreditCard
 * val paymentCards: List<CharSequence>
 * // ✅ Each card number in the list must be valid according to the Luhn algorithm
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.CREDIT_CARD_PATTERN_VIOLATION]
 *   Triggered when a value is not numeric or fails the Luhn checksum.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = CreditCardConstraint::class, validatedBy = [CreditCardValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class CreditCard(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])