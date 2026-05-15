package io.ghaylan.springboot.validation.validators.string

import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.string.phone.PhoneConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.phone.PhoneValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PhoneValidatorTest {

    private val ctx = TestHelper.defaultContext()

    private fun constraint(
        allowedTypes: Set<PhoneNumberType> = emptySet(),
        allowedCountries: Set<String> = emptySet()
    ) = PhoneConstraint(
        allowedTypes = allowedTypes,
        allowedCountries = allowedCountries,
        groups = setOf(OnDefault::class),
        message = ""
    )

    // Google US headquarters: +1-650-253-0000 → digits only: "16502530000"
    // This is a real published number and validates as a US FIXED_LINE_OR_MOBILE in libphonenumber
    private val validUsNumber = "16502530000"

    // French mobile number (06/07 prefix = mobile in France): +33 6 12 34 56 78 → "33612345678"
    private val validFrMobile = "33612345678"

    @Nested
    inner class NullTest {
        @Test fun `null is valid`() = runTest {
            assertNull(PhoneValidator.runValidation(null, constraint(), ctx))
        }
    }

    @Nested
    inner class FormatValidationTest {
        @Test fun `non-digit characters immediately fail`() = runTest {
            val error = PhoneValidator.runValidation("+1-650-253-0000", constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.PHONE_FORMAT_VIOLATION, error?.code)
        }

        @Test fun `number with spaces fails (non-digit)`() = runTest {
            assertNotNull(PhoneValidator.runValidation("1 650 253 0000", constraint(), ctx))
        }

        @Test fun `very short number fails libphonenumber validation`() = runTest {
            val error = PhoneValidator.runValidation("12345", constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.PHONE_FORMAT_VIOLATION, error?.code)
        }

        @Test fun `empty string fails`() = runTest {
            assertNotNull(PhoneValidator.runValidation("", constraint(), ctx))
        }

        @Test fun `valid US number passes`() = runTest {
            assertNull(PhoneValidator.runValidation(validUsNumber, constraint(), ctx))
        }

        @Test fun `valid French mobile passes`() = runTest {
            assertNull(PhoneValidator.runValidation(validFrMobile, constraint(), ctx))
        }
    }

    @Nested
    inner class TypeFilterTest {
        @Test fun `valid number with no type restriction passes`() = runTest {
            assertNull(PhoneValidator.runValidation(validUsNumber, constraint(allowedTypes = emptySet()), ctx))
        }

        @Test fun `valid mobile number passes when MOBILE is allowed`() = runTest {
            // French mobile numbers (06x/07x) are classified as MOBILE by libphonenumber
            assertNull(
                PhoneValidator.runValidation(
                    validFrMobile,
                    constraint(allowedTypes = setOf(PhoneNumberType.MOBILE)),
                    ctx
                )
            )
        }

        @Test fun `mobile number fails when only FIXED_LINE allowed`() = runTest {
            // French mobile classified as MOBILE → fails when only FIXED_LINE is allowed
            val error = PhoneValidator.runValidation(
                validFrMobile,
                constraint(allowedTypes = setOf(PhoneNumberType.FIXED_LINE)),
                ctx
            )
            assertNotNull(error)
            assertEquals(ApiErrorCode.PHONE_TYPE_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class CountryFilterTest {
        @Test fun `US number passes with US country allowed`() = runTest {
            assertNull(
                PhoneValidator.runValidation(
                    validUsNumber,
                    constraint(allowedCountries = setOf("US")),
                    ctx
                )
            )
        }

        @Test fun `US number fails when only FR is allowed`() = runTest {
            val error = PhoneValidator.runValidation(
                validUsNumber,
                constraint(allowedCountries = setOf("FR")),
                ctx
            )
            assertNotNull(error)
            assertEquals(ApiErrorCode.PHONE_COUNTRY_VIOLATION, error?.code)
        }

        @Test fun `French number passes when FR is in allowed countries`() = runTest {
            assertNull(
                PhoneValidator.runValidation(
                    validFrMobile,
                    constraint(allowedCountries = setOf("US", "FR")),
                    ctx
                )
            )
        }

        @Test fun `French number fails when only US is allowed`() = runTest {
            val error = PhoneValidator.runValidation(
                validFrMobile,
                constraint(allowedCountries = setOf("US")),
                ctx
            )
            assertNotNull(error)
            assertEquals(ApiErrorCode.PHONE_COUNTRY_VIOLATION, error?.code)
        }
    }
}
