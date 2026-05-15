package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.password.PasswordConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.password.PasswordValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated string meets configurable password strength requirements:
 * length bounds, required character classes (uppercase, lowercase, digit, special), and minimum Shannon entropy.
 *
 * @property minLength Minimum length (inclusive, default 6).
 * @property maxLength Maximum length (inclusive, default 64).
 * @property requireUppercase Require at least one uppercase letter.
 * @property requireLowercase Require at least one lowercase letter.
 * @property requireDigit Require at least one digit.
 * @property requireSpecialChar Require at least one character from [allowedSpecialChars].
 * @property allowedSpecialChars Characters considered special for the special-char requirement.
 * @property minEntropy Minimum Shannon entropy level; defaults to [PasswordStrength.VERY_WEAK] (any entropy).
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = PasswordConstraint::class, validatedBy = [PasswordValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Password(
	val minLength: Int = 6,
	val maxLength: Int = 64,
	val requireUppercase: Boolean = false,
	val requireLowercase: Boolean = false,
	val requireDigit: Boolean = false,
	val requireSpecialChar: Boolean = false,
	val allowedSpecialChars: String = "!@#$%^&*()-_=+[{]};:,<.>/?",
	val minEntropy: PasswordStrength = PasswordStrength.VERY_WEAK,
	val noSequentialChars: Boolean = false,
	val noRepetitivePatterns: Boolean = false,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
) {
	
	/**
	 * Minimum Shannon entropy thresholds for password strength classification.
	 *
	 * @param entropy Minimum entropy in bits.
	 */
	enum class PasswordStrength(val entropy: Int) {
		VERY_WEAK(0),
		WEAK(28),
		MODERATE(36),
		STRONG(60),
		VERY_STRONG(128)
	}
}