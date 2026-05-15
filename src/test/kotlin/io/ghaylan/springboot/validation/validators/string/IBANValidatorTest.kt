package io.ghaylan.springboot.validation.validators.string

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.string.iban.IBANConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.iban.IBANValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IBANValidatorTest {

    private val ctx = TestHelper.defaultContext()
    private val constraint = IBANConstraint(groups = setOf(OnDefault::class), message = "")

    // Valid IBANs constructed to satisfy the 20-digit checksum algorithm:
    // firstDigits (positions 0..8) and lastDigits (positions 9..19)
    // a = firstDigits.toLong() % 97
    // b = a.toString() + lastDigits
    // valid when b.toLong() % 97 == 0

    @Nested
    inner class ValidIBANTest {
        @Test fun `null is valid`() = runTest {
            assertNull(IBANValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `all-zeros (20 digits) is valid`() = runTest {
            // firstDigits="000000000" → a=0, b="0"+"00000000000"=12 zeros → 0 % 97 == 0
            assertNull(IBANValidator.runValidation("00000000000000000000", constraint, ctx))
        }

        @Test fun `known valid IBAN checksum passes`() = runTest {
            // firstDigits="000000097" → a = 97 % 97 = 0
            // lastDigits="00000000097" → b = "0"+"00000000097" = "000000000097" → 97
            // 97 % 97 == 0 ✓
            assertNull(IBANValidator.runValidation("00000009700000000097", constraint, ctx))
        }

        @Test fun `isValid static method - all zeros`() {
            assertTrue(IBANValidator.isValid("00000000000000000000"))
        }

        @Test fun `isValid static method - known valid`() {
            assertTrue(IBANValidator.isValid("00000009700000000097"))
        }
    }

    @Nested
    inner class InvalidIBANTest {
        @Test fun `wrong length (13 chars) fails`() = runTest {
            val error = IBANValidator.runValidation("1234567890123", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.IBAN_FORMAT_VIOLATION, error?.code)
        }

        @Test fun `wrong length (21 chars) fails`() = runTest {
            assertNotNull(IBANValidator.runValidation("000000000000000000000", constraint, ctx))
        }

        @Test fun `wrong length (19 chars) fails`() = runTest {
            assertNotNull(IBANValidator.runValidation("0000000000000000000", constraint, ctx))
        }

        @Test fun `bad checksum fails`() = runTest {
            // firstDigits="000000000" → a=0, lastDigits="00000000001" → b="000000000001" → 1 % 97 != 0
            val error = IBANValidator.runValidation("00000000000000000001", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.IBAN_FORMAT_VIOLATION, error?.code)
        }

        @Test fun `non-numeric characters fail`() = runTest {
            // length 20 but not parseable as long
            assertNotNull(IBANValidator.runValidation("IBAN0000000000000000", constraint, ctx))
        }

        @Test fun `blank string fails`() = runTest {
            assertNotNull(IBANValidator.runValidation("   ", constraint, ctx))
        }

        @Test fun `empty string fails`() = runTest {
            assertNotNull(IBANValidator.runValidation("", constraint, ctx))
        }

        @Test fun `isValid static method - wrong length`() {
            assertFalse(IBANValidator.isValid("12345"))
        }

        @Test fun `isValid static method - bad checksum`() {
            assertFalse(IBANValidator.isValid("00000000000000000001"))
        }

        @Test fun `isValid static method - null returns false`() {
            assertFalse(IBANValidator.isValid(null))
        }
    }
}
