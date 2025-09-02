package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.iban.IBANConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.iban.IBANValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence represents a valid International Bank Account Number (IBAN),
 * or that each element in an array or collection is a valid IBAN.
 *
 * ### Description
 * The `@IBAN` annotation ensures that the annotated value (or each element in a collection)
 * conforms to the IBAN structure and passes checksum validation based on ISO 13616 standards.
 * This is typically used to validate bank account identifiers in payment, transfer,
 * or financial account registration systems.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation fails if:
 *   - The IBAN is not exactly 20 characters long.
 *   - The IBAN does not pass the modulo 97 checksum validation.
 * - For arrays or collections, each element must individually be a valid IBAN.
 *
 * ### Example Usage
 * ```kotlin
 * @IBAN
 * val accountIban: String
 * // ✅ accountIban must be a valid IBAN string
 *
 * -----
 *
 * @IBAN
 * val transferIbans: List<CharSequence>
 * // ✅ each value in transferIbans must be a valid IBAN string
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.IBAN_FORMAT_VIOLATION]
 *   Triggered when the value does not meet IBAN format or checksum requirements.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = IBANConstraint::class, validatedBy = [IBANValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class IBAN(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])