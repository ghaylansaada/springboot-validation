package io.ghaylan.springboot.validation.constraints.annotations

import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.phone.PhoneConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.phone.PhoneValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence contains a valid international phone number.
 *
 * ### Description
 * The `@Phone` annotation uses Google's libphonenumber library to validate phone numbers
 * according to international standards. It supports filtering by phone number types
 * (e.g., MOBILE, FIXED_LINE) and restricting validation to specific allowed country codes.
 * This annotation can be applied to single values or collections/arrays of values,
 * validating each element individually.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The phone number must be valid in any recognized format (international, national, E.164, RFC3966).
 * - If `allowedTypes` is specified, the phone number must match one of these types.
 * - If `allowedCountries` is specified, the phone number's country code must be included in this list.
 *
 * ### Example Usage
 * ```kotlin
 *
 * @Phone
 * val phoneNumber: String
 * // ✅ phoneNumber must be a valid phone number
 *
 * -----
 *
 * @Phone(allowedTypes = [PhoneNumberType.MOBILE])
 * val mobileNumber: String
 * // ✅ mobileNumber must be a valid mobile phone number only
 *
 * -----
 *
 * @Phone(allowedCountries = ["US", "FR"])
 * val allowedCountryNumber: String
 * // ✅ allowedCountryNumber must be a valid phone number from US or FR
 *
 * -----
 *
 * @Phone
 * val phoneArray: Array<String>
 * // ✅ each value in phoneArray must be a valid phone number
 *
 * -----
 *
 * @Phone(allowedCountries = ["DE", "IT"])
 * val europeanNumbers: List<String>
 * // ✅ each value in europeanNumbers must be a valid phone number from DE or IT
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PHONE_FORMAT_VIOLATION]
 *   Triggered when the phone number format is invalid.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PHONE_TYPE_VIOLATION]
 *   Triggered when the phone number type does not match any allowed types.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PHONE_COUNTRY_VIOLATION]
 *   Triggered when the phone number's country code is not in the allowed countries.
 *
 * @property allowedTypes The allowed phone number types (e.g., MOBILE, FIXED_LINE).
 * Leave empty to allow any type.
 * @property allowedCountries The allowed ISO 3166-1 alpha-2 country codes (e.g., "US", "FR").
 * Leave empty to allow numbers from any country.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = PhoneConstraint::class, validatedBy = [PhoneValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Phone(
    val allowedTypes: Array<PhoneNumberUtil.PhoneNumberType> = [],
    val allowedCountries: Array<String> = [],
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])