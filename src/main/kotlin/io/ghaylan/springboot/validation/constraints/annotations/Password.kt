package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.password.PasswordConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.password.PasswordValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence or each element in a collection
 * meets configurable password strength requirements.
 *
 * ### Description
 * The `@Password` annotation enforces customizable password rules on the annotated value,
 * which must be a character sequence such as a `String` or `CharSequence`,
 * or a collection/array of such elements. It validates constraints such as length boundaries,
 * required character classes (uppercase, lowercase, digit, special),
 * allowed special characters, and minimum Shannon entropy to ensure password strength and complexity.
 *
 * This annotation is useful for user registration, credential updates, or any scenario requiring
 * secure password validation with flexible policy parameters.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Password length must be within the configured `minLength` and `maxLength`.
 * - If enabled, the password must include at least one uppercase letter.
 * - If enabled, the password must include at least one lowercase letter.
 * - If enabled, the password must include at least one digit.
 * - If enabled, the password must include at least one allowed special character from `allowedSpecialChars`.
 * - Password entropy must meet or exceed the minimum required Shannon entropy (`minEntropy`).
 * - (Additional rules such as `noSequentialChars` and `noRepetitivePatterns` may be implemented but are not currently validated by `PasswordValidator`.)
 *
 * ### Example Usage
 * ```kotlin
 * @Password(minLength = 8, maxLength = 32, requireUppercase = true, requireDigit = true)
 * val userPassword: String
 * // ✅ userPassword must be 8-32 characters, include uppercase letters and digits
 *
 * -----
 *
 * @Password(
 *     minLength = 12,
 *     requireUppercase = true,
 *     requireLowercase = true,
 *     requireDigit = true,
 *     requireSpecialChar = true,
 *     allowedSpecialChars = "!@#\$%^&*",
 *     minEntropy = Password.PasswordStrength.STRONG)
 * val adminPasswords: List<String>
 * // ✅ each password in adminPasswords must be at least 12 characters,
 * //    include uppercase, lowercase, digits, special chars from "!@#$%^&*",
 * //    and have strong entropy (60+ bits)
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PASSWORD_LENGTH_VIOLATION]
 *   Must be between `minLength` and `maxLength` characters.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PASSWORD_UPPERCASE_VIOLATION]
 *   Must include at least one uppercase letter.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PASSWORD_LOWERCASE_VIOLATION]
 *   Must include at least one lowercase letter.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PASSWORD_DIGIT_VIOLATION]
 *   Must include at least one digit.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PASSWORD_SPECIAL_CHAR_VIOLATION]
 *   Must include at least one of the allowed special characters.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PASSWORD_ENTROPY_VIOLATION]
 *   Entropy must be at least the configured minimum in bits.
 *
 * @property minLength The minimum allowed password length (inclusive). Must be ≥ 0.
 * @property maxLength The maximum allowed password length (inclusive).
 * @property requireUppercase Whether at least one uppercase letter is required.
 * @property requireLowercase Whether at least one lowercase letter is required.
 * @property requireDigit Whether at least one digit is required.
 * @property requireSpecialChar Whether at least one allowed special character is required.
 * @property allowedSpecialChars String of characters allowed as special characters.
 * @property minEntropy The minimum Shannon entropy strength required, from [PasswordStrength].
 * @property noSequentialChars If true, passwords with sequential characters are disallowed (not validated by default).
 * @property noRepetitivePatterns If true, passwords with repetitive patterns are disallowed (not validated by default).
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = PasswordConstraint::class, validatedBy = [PasswordValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Password(
    val minLength : Int = 6,
    val maxLength : Int = 64,
    val requireUppercase : Boolean = false,
    val requireLowercase : Boolean = false,
    val requireDigit : Boolean = false,
    val requireSpecialChar: Boolean = false,
    val allowedSpecialChars : String = "!@#$%^&*()-_=+[{]};:,<.>/?",
    val minEntropy : PasswordStrength = PasswordStrength.VERY_WEAK,
    val noSequentialChars : Boolean = false,
    val noRepetitivePatterns : Boolean = false,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])
{
	/**
	 * Defines password strength levels based on Shannon entropy.
	 *
	 * **Description**:
	 * This enum categorizes password strength based on calculated Shannon entropy,
	 * which measures randomness and unpredictability. Higher entropy indicates a stronger password.
	 *
	 * **Entropy Calculation**:
	 * Entropy is calculated using:
	 * - Character set size (e.g., lowercase, uppercase, digits, special characters).
	 * - Password length.
	 * - Character distribution.
	 *
	 * @param entropy The minimum Shannon entropy required in bits.
	 */
	enum class PasswordStrength(val entropy : Int)
	{
		/** Very weak passwords with minimal entropy (0+ bits) */
		VERY_WEAK(0),

		/** Weak passwords with basic entropy (28+ bits) */
		WEAK(28),

		/** Moderately strong passwords (36+ bits) */
		MODERATE(36),

		/** Strong passwords suitable for most security needs (60+ bits) */
		STRONG(60),

		/** Very strong passwords for high-security applications (128+ bits) */
		VERY_STRONG(128)
	}
}