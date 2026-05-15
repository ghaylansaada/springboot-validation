package io.ghaylan.springboot.validation.validators.string

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.annotations.Password.PasswordStrength
import io.ghaylan.springboot.validation.constraints.validators.string.password.PasswordConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.password.PasswordValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PasswordValidatorTest {

    private val ctx = TestHelper.defaultContext()

    private fun constraint(
        minLength: Int = 6,
        maxLength: Int = 64,
        requireUppercase: Boolean = false,
        requireLowercase: Boolean = false,
        requireDigit: Boolean = false,
        requireSpecialChar: Boolean = false,
        allowedSpecialChars: String = "!@#\$%^&*()-_=+[{]};:,<.>/?",
        minEntropy: PasswordStrength = PasswordStrength.VERY_WEAK,
    ) = PasswordConstraint(
        minLength = minLength, maxLength = maxLength,
        requireUppercase = requireUppercase, requireLowercase = requireLowercase,
        requireDigit = requireDigit, requireSpecialChar = requireSpecialChar,
        allowedSpecialChars = allowedSpecialChars, minEntropy = minEntropy,
        noSequentialChars = false, noRepetitivePatterns = false,
        groups = setOf(OnDefault::class), message = ""
    )

    @Test fun `null is valid`() = runTest {
        assertNull(PasswordValidator.runValidation(null, constraint(), ctx))
    }

    @Test fun `valid simple password`() = runTest {
        assertNull(PasswordValidator.runValidation("password", constraint(), ctx))
    }

    @Test fun `too short password`() = runTest {
        val error = PasswordValidator.runValidation("abc", constraint(minLength = 6), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.PASSWORD_LENGTH_VIOLATION, error?.code)
    }

    @Test fun `too long password`() = runTest {
        val error = PasswordValidator.runValidation("a".repeat(100), constraint(maxLength = 64), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.PASSWORD_LENGTH_VIOLATION, error?.code)
    }

    @Test fun `at min length boundary`() = runTest {
        assertNull(PasswordValidator.runValidation("abcdef", constraint(minLength = 6), ctx))
    }

    @Test fun `at max length boundary`() = runTest {
        assertNull(PasswordValidator.runValidation("a".repeat(64), constraint(maxLength = 64), ctx))
    }

    @Test fun `missing uppercase when required`() = runTest {
        val error = PasswordValidator.runValidation("password123", constraint(requireUppercase = true), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.PASSWORD_UPPERCASE_VIOLATION, error?.code)
    }

    @Test fun `has uppercase when required`() = runTest {
        assertNull(PasswordValidator.runValidation("Password123", constraint(requireUppercase = true), ctx))
    }

    @Test fun `missing lowercase when required`() = runTest {
        val error = PasswordValidator.runValidation("PASSWORD123", constraint(requireLowercase = true), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.PASSWORD_LOWERCASE_VIOLATION, error?.code)
    }

    @Test fun `has lowercase when required`() = runTest {
        assertNull(PasswordValidator.runValidation("Password", constraint(requireLowercase = true), ctx))
    }

    @Test fun `missing digit when required`() = runTest {
        val error = PasswordValidator.runValidation("Password", constraint(requireDigit = true), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.PASSWORD_DIGIT_VIOLATION, error?.code)
    }

    @Test fun `has digit when required`() = runTest {
        assertNull(PasswordValidator.runValidation("Password1", constraint(requireDigit = true), ctx))
    }

    @Test fun `missing special char when required`() = runTest {
        val error = PasswordValidator.runValidation("Password1", constraint(requireSpecialChar = true), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.PASSWORD_SPECIAL_CHAR_VIOLATION, error?.code)
    }

    @Test fun `has special char when required`() = runTest {
        assertNull(PasswordValidator.runValidation("Password1!", constraint(requireSpecialChar = true), ctx))
    }

    @Test fun `weak entropy fails for STRONG requirement`() = runTest {
        val error = PasswordValidator.runValidation("aaaaaa", constraint(minEntropy = PasswordStrength.STRONG), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.PASSWORD_ENTROPY_VIOLATION, error?.code)
    }

    @Test fun `strong password passes MODERATE requirement`() = runTest {
        assertNull(PasswordValidator.runValidation("MyP@ssw0rd12", constraint(minEntropy = PasswordStrength.MODERATE), ctx))
    }

    @Test fun `first failing check wins - length before uppercase`() = runTest {
        val error = PasswordValidator.runValidation("ab", constraint(minLength = 6, requireUppercase = true), ctx)
        assertEquals(ApiErrorCode.PASSWORD_LENGTH_VIOLATION, error?.code)
    }
}
