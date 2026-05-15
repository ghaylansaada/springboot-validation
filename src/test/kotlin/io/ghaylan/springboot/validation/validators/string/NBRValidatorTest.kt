package io.ghaylan.springboot.validation.validators.string

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.string.nbr.NBRConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.nbr.NBRValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NBRValidatorTest {

    private val ctx = TestHelper.defaultContext()
    private val constraint = NBRConstraint(groups = setOf(OnDefault::class), message = "")

    // NBR algorithm: padStart(8, '0'), capitalizeLastChar(), match [0-9]{7}[ABCDEFGHJKLMNPQRSTVWXYZ]
    // checksum: sum(digit[i] * (7-i)) for i in 0..6, then % 23, then map to checksum alphabet
    // Alphabet: "ABCDEFGHJKLMNPQRSTVWXYZ" (23 chars, missing I, O, U)
    //
    // "0000000A" → digits "0000000" → sum = 0 → 0 % 23 = 0 → 'A' ✓
    // "0000001B" → digits "0000001" → 1*1=1 → 1 % 23 = 1 → 'B' ✓
    // "0000023E" → digits "0000023" → 0+0+0+0+0+2*2+3*1 = 7 → 7 % 23 = 7 → alphabet[7]='H'... wait let me recalc
    // Actually: mapIndexed { index, char -> char.digitToInt() * (7 - index) }
    // For "0000000": 0*7+0*6+0*5+0*4+0*3+0*2+0*1 = 0 → 0%23=0 → 'A'
    // For "0000001": 0*7+0*6+0*5+0*4+0*3+0*2+1*1 = 1 → 1%23=1 → 'B'

    @Nested
    inner class ValidNBRTest {
        @Test fun `null is valid`() = runTest {
            assertNull(NBRValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `8-char full-length valid NBR (all zeros + A)`() = runTest {
            // "0000000A" → sum=0 → 0%23=0 → 'A' ✓
            assertNull(NBRValidator.runValidation("0000000A", constraint, ctx))
        }

        @Test fun `2-char minimum length valid NBR`() = runTest {
            // "0A" pads to "0000000A" → sum=0 → 'A' ✓
            assertNull(NBRValidator.runValidation("0A", constraint, ctx))
        }

        @Test fun `valid NBR with sum = 1 → letter B`() = runTest {
            // "0000001B" → sum=1 → 1%23=1 → 'B' ✓
            assertNull(NBRValidator.runValidation("0000001B", constraint, ctx))
        }

        @Test fun `lowercase last char is accepted (capitalizeLastChar)`() = runTest {
            // "0000000a" → capitalizeLastChar → "0000000A" → valid
            assertNull(NBRValidator.runValidation("0000000a", constraint, ctx))
        }

        @Test fun `2-char lowercase valid`() = runTest {
            // "0a" pads to "0000000a" → capitalizes to "0000000A" → valid
            assertNull(NBRValidator.runValidation("0a", constraint, ctx))
        }

        @Test fun `isValid static method - valid case`() {
            assertTrue(NBRValidator.isValid("0000000A"))
        }

        @Test fun `isValid static method - minimum length valid`() {
            assertTrue(NBRValidator.isValid("0A"))
        }
    }

    @Nested
    inner class InvalidNBRTest {
        @Test fun `too short (1 char) fails`() = runTest {
            val error = NBRValidator.runValidation("A", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.NBR_FORMAT_VIOLATION, error?.code)
        }

        @Test fun `too long (9 chars) fails`() = runTest {
            assertNotNull(NBRValidator.runValidation("000000000", constraint, ctx))
        }

        @Test fun `wrong checksum letter fails`() = runTest {
            // "0000000B" → expected 'A' but 'B' given
            assertNotNull(NBRValidator.runValidation("0000000B", constraint, ctx))
        }

        @Test fun `disallowed letter I fails`() = runTest {
            // 'I' is not in the checksum alphabet
            assertNotNull(NBRValidator.runValidation("0000000I", constraint, ctx))
        }

        @Test fun `disallowed letter O fails`() = runTest {
            assertNotNull(NBRValidator.runValidation("0000000O", constraint, ctx))
        }

        @Test fun `disallowed letter U fails`() = runTest {
            assertNotNull(NBRValidator.runValidation("0000000U", constraint, ctx))
        }

        @Test fun `all-digit (no letter) fails pattern match`() = runTest {
            // "00000000" → last char '0' is not in the checksum alphabet after capitalize
            assertNotNull(NBRValidator.runValidation("00000000", constraint, ctx))
        }

        @Test fun `empty string fails`() = runTest {
            assertNotNull(NBRValidator.runValidation("", constraint, ctx))
        }

        @Test fun `isValid static method - wrong checksum`() {
            assertFalse(NBRValidator.isValid("0000000B"))
        }

        @Test fun `isValid static method - too short`() {
            assertFalse(NBRValidator.isValid("A"))
        }

        @Test fun `isValid static method - too long`() {
            assertFalse(NBRValidator.isValid("000000000"))
        }
    }
}
