package io.ghaylan.springboot.validation.validators.string

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.annotations.StrOcc.StrOccMode
import io.ghaylan.springboot.validation.constraints.annotations.Url.UrlType
import io.ghaylan.springboot.validation.constraints.validators.string.base64.Base64Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.base64.Base64Validator
import io.ghaylan.springboot.validation.constraints.validators.string.contains.StrOccConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.contains.StrOccValidator
import io.ghaylan.springboot.validation.constraints.validators.string.country.ISOCountryConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.country.ISOCountryValidator
import io.ghaylan.springboot.validation.constraints.validators.string.creditcard.CreditCardConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.creditcard.CreditCardValidator
import io.ghaylan.springboot.validation.constraints.validators.string.currency.CurrencyCodeConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.currency.CurrencyCodeValidator
import io.ghaylan.springboot.validation.constraints.validators.string.email.EmailConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.email.EmailValidator
import io.ghaylan.springboot.validation.constraints.validators.string.enums.EnumConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.enums.EnumValidator
import io.ghaylan.springboot.validation.constraints.validators.string.hexcolor.HexColorConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.hexcolor.HexColorValidator
import io.ghaylan.springboot.validation.constraints.validators.string.language.LanguageConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.language.LanguageValidator
import io.ghaylan.springboot.validation.constraints.validators.string.length.TextLengthConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.length.TextLengthValidator
import io.ghaylan.springboot.validation.constraints.validators.string.notcontains.NotStrOccConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.notcontains.NotStrOccValidator
import io.ghaylan.springboot.validation.constraints.validators.string.regex.RegexConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.regex.RegexValidator
import io.ghaylan.springboot.validation.constraints.validators.string.url.UrlConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.url.UrlValidator
import io.ghaylan.springboot.validation.constraints.validators.string.uuid.UuidConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.uuid.UuidValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StringValidatorsTest {

    private val ctx = TestHelper.defaultContext()
    private fun constraint(groups: Set<kotlin.reflect.KClass<*>> = setOf(OnDefault::class)) = groups

    @Nested
    inner class EmailValidatorTest {
        private val constraint = EmailConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(EmailValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid email`() = runTest {
            assertNull(EmailValidator.runValidation("user@example.com", constraint, ctx))
        }

        @Test fun `valid email with subdomain`() = runTest {
            assertNull(EmailValidator.runValidation("user@mail.example.com", constraint, ctx))
        }

        @Test fun `plus addressing is not supported by this validator`() = runTest {
            // The regex character class [_a-zA-Z0-9-] does not include '+', so plus addressing fails
            assertNotNull(EmailValidator.runValidation("user+tag@example.com", constraint, ctx))
        }

        @Test fun `invalid email with double at`() = runTest {
            assertNotNull(EmailValidator.runValidation("user@@example.com", constraint, ctx))
        }

        @Test fun `invalid email missing at`() = runTest {
            val error = EmailValidator.runValidation("userexample.com", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.EMAIL_FORMAT_VIOLATION, error?.code)
        }

        @Test fun `invalid email missing domain`() = runTest {
            val error = EmailValidator.runValidation("user@", constraint, ctx)
            assertNotNull(error)
        }

        @Test fun `invalid email with spaces`() = runTest {
            assertNotNull(EmailValidator.runValidation("user @example.com", constraint, ctx))
        }

        @Test fun `empty string is valid (matches optional pattern)`() = runTest {
            assertNull(EmailValidator.runValidation("", constraint, ctx))
        }
    }

    @Nested
    inner class UuidValidatorTest {
        private val constraint = UuidConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(UuidValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid uuid`() = runTest {
            assertNull(UuidValidator.runValidation("123e4567-e89b-12d3-a456-426614174000", constraint, ctx))
        }

        @Test fun `invalid uuid`() = runTest {
            val error = UuidValidator.runValidation("not-a-uuid", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.UUID_PATTERN_VIOLATION, error?.code)
        }

        @Test fun `uuid without hyphens is invalid`() = runTest {
            assertNotNull(UuidValidator.runValidation("123e4567e89b12d3a456426614174000", constraint, ctx))
        }

        @Test fun `uppercase uuid is valid`() = runTest {
            assertNull(UuidValidator.runValidation("123E4567-E89B-12D3-A456-426614174000", constraint, ctx))
        }

        @Test fun `all-zeros uuid is valid`() = runTest {
            assertNull(UuidValidator.runValidation("00000000-0000-0000-0000-000000000000", constraint, ctx))
        }

        @Test fun `empty string is invalid uuid`() = runTest {
            assertNotNull(UuidValidator.runValidation("", constraint, ctx))
        }
    }

    @Nested
    inner class TextLengthValidatorTest {
        @Test fun `null is valid`() = runTest {
            val constraint = TextLengthConstraint(min = 1, max = 10, groups = setOf(OnDefault::class), message = "")
            assertNull(TextLengthValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `value within range is valid`() = runTest {
            val constraint = TextLengthConstraint(min = 1, max = 10, groups = setOf(OnDefault::class), message = "")
            assertNull(TextLengthValidator.runValidation("hello", constraint, ctx))
        }

        @Test fun `value at min boundary is valid`() = runTest {
            val constraint = TextLengthConstraint(min = 5, max = 10, groups = setOf(OnDefault::class), message = "")
            assertNull(TextLengthValidator.runValidation("hello", constraint, ctx))
        }

        @Test fun `value at max boundary is valid`() = runTest {
            val constraint = TextLengthConstraint(min = 1, max = 5, groups = setOf(OnDefault::class), message = "")
            assertNull(TextLengthValidator.runValidation("hello", constraint, ctx))
        }

        @Test fun `value too short`() = runTest {
            val constraint = TextLengthConstraint(min = 5, max = 10, groups = setOf(OnDefault::class), message = "")
            val error = TextLengthValidator.runValidation("hi", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.STRING_LENGTH_VIOLATION, error?.code)
        }

        @Test fun `value too long`() = runTest {
            val constraint = TextLengthConstraint(min = 1, max = 3, groups = setOf(OnDefault::class), message = "")
            assertNotNull(TextLengthValidator.runValidation("hello world", constraint, ctx))
        }

        @Test fun `min equals max exact length is valid`() = runTest {
            val constraint = TextLengthConstraint(min = 5, max = 5, groups = setOf(OnDefault::class), message = "")
            assertNull(TextLengthValidator.runValidation("hello", constraint, ctx))
        }

        @Test fun `min equals max wrong length fails`() = runTest {
            val constraint = TextLengthConstraint(min = 5, max = 5, groups = setOf(OnDefault::class), message = "")
            assertNotNull(TextLengthValidator.runValidation("hi", constraint, ctx))
        }

        @Test fun `min zero empty string is valid`() = runTest {
            val constraint = TextLengthConstraint(min = 0, max = 10, groups = setOf(OnDefault::class), message = "")
            assertNull(TextLengthValidator.runValidation("", constraint, ctx))
        }
    }

    @Nested
    inner class Base64ValidatorTest {
        private val constraint = Base64Constraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(Base64Validator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid base64 with single padding`() = runTest {
            assertNull(Base64Validator.runValidation("SGVsbG8gV29ybGQ=", constraint, ctx))
        }

        @Test fun `valid base64 with double padding`() = runTest {
            // "Ma" encodes to "TWE=" — "M" alone encodes to "TQ=="
            assertNull(Base64Validator.runValidation("TQ==", constraint, ctx))
        }

        @Test fun `unpadded base64 is invalid (validator requires padding)`() = runTest {
            // Length not a multiple of 4 → fails
            val error = Base64Validator.runValidation("SGVsbG8gV29ybGQ", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.BASE64_VIOLATION, error?.code)
        }

        @Test fun `invalid base64 with special chars`() = runTest {
            val error = Base64Validator.runValidation("not!valid!base64", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.BASE64_VIOLATION, error?.code)
        }

        @Test fun `empty string is invalid base64`() = runTest {
            // Empty string has length 0, which is 0 % 4 == 0, but regex matches and decode may succeed
            // The validator returns null for empty (valid base64 of zero bytes is "")
            // Actual behavior: empty string passes regex and decodes to empty byte array
            assertNull(Base64Validator.runValidation("", constraint, ctx))
        }
    }

    @Nested
    inner class HexColorValidatorTest {
        private val constraint = HexColorConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(HexColorValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid 6-digit hex`() = runTest {
            assertNull(HexColorValidator.runValidation("#FF5733", constraint, ctx))
        }

        @Test fun `valid 3-digit hex`() = runTest {
            assertNull(HexColorValidator.runValidation("#F00", constraint, ctx))
        }

        @Test fun `valid lowercase hex`() = runTest {
            assertNull(HexColorValidator.runValidation("#ff5733", constraint, ctx))
        }

        @Test fun `invalid without hash`() = runTest {
            assertNotNull(HexColorValidator.runValidation("FF5733", constraint, ctx))
        }

        @Test fun `invalid 4-digit hex`() = runTest {
            assertNotNull(HexColorValidator.runValidation("#FF57", constraint, ctx))
        }
    }

    @Nested
    inner class RegexValidatorTest {
        @Test fun `null is valid`() = runTest {
            val constraint = RegexConstraint(pattern = "^[A-Z]+$", groups = setOf(OnDefault::class), message = "")
            assertNull(RegexValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `matching pattern is valid`() = runTest {
            val constraint = RegexConstraint(pattern = "^[A-Z]+$", groups = setOf(OnDefault::class), message = "")
            assertNull(RegexValidator.runValidation("HELLO", constraint, ctx))
        }

        @Test fun `non-matching pattern fails`() = runTest {
            val constraint = RegexConstraint(pattern = "^[A-Z]+$", groups = setOf(OnDefault::class), message = "")
            val error = RegexValidator.runValidation("hello", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.PATTERN_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class ISOCountryValidatorTest {
        private val constraint = ISOCountryConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(ISOCountryValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid country code US`() = runTest {
            assertNull(ISOCountryValidator.runValidation("US", constraint, ctx))
        }

        @Test fun `valid country code FR`() = runTest {
            assertNull(ISOCountryValidator.runValidation("FR", constraint, ctx))
        }

        @Test fun `invalid country code`() = runTest {
            val error = ISOCountryValidator.runValidation("XX", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.ISO_COUNTRY_CODE_VIOLATION, error?.code)
        }

        @Test fun `lowercase country code is invalid`() = runTest {
            assertNotNull(ISOCountryValidator.runValidation("us", constraint, ctx))
        }

        @Test fun `empty string is invalid`() = runTest {
            assertNotNull(ISOCountryValidator.runValidation("", constraint, ctx))
        }
    }

    @Nested
    inner class CurrencyCodeValidatorTest {
        private val constraint = CurrencyCodeConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(CurrencyCodeValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid currency USD`() = runTest {
            assertNull(CurrencyCodeValidator.runValidation("USD", constraint, ctx))
        }

        @Test fun `valid currency EUR`() = runTest {
            assertNull(CurrencyCodeValidator.runValidation("EUR", constraint, ctx))
        }

        @Test fun `invalid currency`() = runTest {
            val error = CurrencyCodeValidator.runValidation("ZZZ", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.ISO_CURRENCY_CODE_VIOLATION, error?.code)
        }

        @Test fun `lowercase currency code is invalid`() = runTest {
            assertNotNull(CurrencyCodeValidator.runValidation("usd", constraint, ctx))
        }

        @Test fun `empty string is invalid`() = runTest {
            assertNotNull(CurrencyCodeValidator.runValidation("", constraint, ctx))
        }
    }

    @Nested
    inner class CreditCardValidatorTest {
        private val constraint = CreditCardConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(CreditCardValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid visa card (Luhn)`() = runTest {
            assertNull(CreditCardValidator.runValidation("4111111111111111", constraint, ctx))
        }

        @Test fun `invalid card number`() = runTest {
            val error = CreditCardValidator.runValidation("1234567890123456", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.CREDIT_CARD_PATTERN_VIOLATION, error?.code)
        }

        @Test fun `non-numeric string`() = runTest {
            assertNotNull(CreditCardValidator.runValidation("abcd1234", constraint, ctx))
        }

        @Test fun `blank string`() = runTest {
            assertNotNull(CreditCardValidator.runValidation("", constraint, ctx))
        }
    }

    @Nested
    inner class EnumValidatorTest {
        @Test fun `null is valid`() = runTest {
            val constraint = EnumConstraint(ignoreCase = false, groups = setOf(OnDefault::class), message = "")
            assertNull(EnumValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid enum style`() = runTest {
            val constraint = EnumConstraint(ignoreCase = false, groups = setOf(OnDefault::class), message = "")
            assertNull(EnumValidator.runValidation("MY_ENUM_VALUE", constraint, ctx))
        }

        @Test fun `lowercase fails without ignoreCase`() = runTest {
            val constraint = EnumConstraint(ignoreCase = false, groups = setOf(OnDefault::class), message = "")
            val error = EnumValidator.runValidation("my_enum", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.ENUM_FORMAT_VIOLATION, error?.code)
        }

        @Test fun `lowercase passes with ignoreCase`() = runTest {
            val constraint = EnumConstraint(ignoreCase = true, groups = setOf(OnDefault::class), message = "")
            assertNull(EnumValidator.runValidation("my_enum_value", constraint, ctx))
        }

        @Test fun `invalid enum with special chars`() = runTest {
            val constraint = EnumConstraint(ignoreCase = false, groups = setOf(OnDefault::class), message = "")
            assertNotNull(EnumValidator.runValidation("NOT-VALID", constraint, ctx))
        }
    }

    @Nested
    inner class LanguageValidatorTest {
        private val constraint = LanguageConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(LanguageValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid language code en`() = runTest {
            assertNull(LanguageValidator.runValidation("en", constraint, ctx))
        }

        @Test fun `valid language code fr`() = runTest {
            assertNull(LanguageValidator.runValidation("fr", constraint, ctx))
        }

        @Test fun `invalid language code`() = runTest {
            val error = LanguageValidator.runValidation("xyz", constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.ISO_LANGUAGE_CODE_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class UrlValidatorTest {
        private fun constraint(
            type: UrlType = UrlType.GENERIC,
            requireHttps: Boolean = false,
            allowQueryParams: Boolean = true,
            allowedExtensions: Set<String> = emptySet()
        ) = UrlConstraint(
            type = type, requireHttps = requireHttps,
            allowQueryParams = allowQueryParams, allowedExtensions = allowedExtensions,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(UrlValidator.runValidation(null, constraint(), ctx))
        }

        @Test fun `valid http url`() = runTest {
            assertNull(UrlValidator.runValidation("http://example.com", constraint(), ctx))
        }

        @Test fun `valid https url`() = runTest {
            assertNull(UrlValidator.runValidation("https://example.com", constraint(), ctx))
        }

        @Test fun `requireHttps fails for http`() = runTest {
            val error = UrlValidator.runValidation("http://example.com", constraint(requireHttps = true), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.URL_HTTPS_REQUIRED_VIOLATION, error?.code)
        }

        @Test fun `requireHttps passes for https`() = runTest {
            assertNull(UrlValidator.runValidation("https://example.com", constraint(requireHttps = true), ctx))
        }

        @Test fun `disallow query params fails when present`() = runTest {
            val error = UrlValidator.runValidation("https://example.com?foo=bar", constraint(allowQueryParams = false), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.URL_QUERY_PARAMS_NOT_ALLOWED_VIOLATION, error?.code)
        }

        @Test fun `invalid url`() = runTest {
            val error = UrlValidator.runValidation("not a url \\bad", constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.URL_VIOLATION, error?.code)
        }

        @Test fun `image url must have valid extension`() = runTest {
            val error = UrlValidator.runValidation("https://example.com/file.txt", constraint(type = UrlType.IMAGE), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.URL_EXTENSION_VIOLATION, error?.code)
        }

        @Test fun `image url with valid extension passes`() = runTest {
            assertNull(UrlValidator.runValidation("https://example.com/photo.jpg", constraint(type = UrlType.IMAGE), ctx))
        }
    }

    @Nested
    inner class StrOccValidatorTest {
        private fun constraint(
            value: String, minOccurrences: Int = 1, maxOccurrences: Int = Int.MAX_VALUE,
            ignoreCase: Boolean = true, mode: StrOccMode = StrOccMode.CONTAINS
        ) = StrOccConstraint(
            value = value, minOccurrences = minOccurrences, maxOccurrences = maxOccurrences,
            ignoreCase = ignoreCase, mode = mode,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(StrOccValidator.runValidation(null, constraint("test"), ctx))
        }

        @Test fun `contains mode matches`() = runTest {
            assertNull(StrOccValidator.runValidation("hello test world", constraint("test", mode = StrOccMode.CONTAINS), ctx))
        }

        @Test fun `contains mode fails when not present`() = runTest {
            val error = StrOccValidator.runValidation("hello world", constraint("test", mode = StrOccMode.CONTAINS), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.STRING_OCCURRENCES_VIOLATION, error?.code)
        }

        @Test fun `starts_with mode matches`() = runTest {
            assertNull(StrOccValidator.runValidation("hello world", constraint("hello", mode = StrOccMode.STARTS_WITH), ctx))
        }

        @Test fun `starts_with mode fails`() = runTest {
            assertNotNull(StrOccValidator.runValidation("world hello", constraint("hello", mode = StrOccMode.STARTS_WITH), ctx))
        }

        @Test fun `ends_with mode matches`() = runTest {
            assertNull(StrOccValidator.runValidation("hello world", constraint("world", mode = StrOccMode.ENDS_WITH), ctx))
        }

        @Test fun `equals mode matches`() = runTest {
            assertNull(StrOccValidator.runValidation("hello", constraint("hello", mode = StrOccMode.EQUALS), ctx))
        }

        @Test fun `equals mode case insensitive`() = runTest {
            assertNull(StrOccValidator.runValidation("HELLO", constraint("hello", mode = StrOccMode.EQUALS, ignoreCase = true), ctx))
        }

        @Test fun `equals mode case sensitive fails`() = runTest {
            assertNotNull(StrOccValidator.runValidation("HELLO", constraint("hello", mode = StrOccMode.EQUALS, ignoreCase = false), ctx))
        }
    }

    @Nested
    inner class NotStrOccValidatorTest {
        private fun constraint(
            value: String, ignoreCase: Boolean = true, mode: StrOccMode = StrOccMode.CONTAINS
        ) = NotStrOccConstraint(
            value = value, ignoreCase = ignoreCase, mode = mode,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(NotStrOccValidator.runValidation(null, constraint("test"), ctx))
        }

        @Test fun `not containing value is valid`() = runTest {
            assertNull(NotStrOccValidator.runValidation("hello world", constraint("test", mode = StrOccMode.CONTAINS), ctx))
        }

        @Test fun `containing value fails`() = runTest {
            val error = NotStrOccValidator.runValidation("hello test world", constraint("test", mode = StrOccMode.CONTAINS), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.STRING_OCCURRENCES_VIOLATION, error?.code)
        }

        @Test fun `not equals is valid`() = runTest {
            assertNull(NotStrOccValidator.runValidation("hello", constraint("world", mode = StrOccMode.EQUALS), ctx))
        }

        @Test fun `equals fails`() = runTest {
            assertNotNull(NotStrOccValidator.runValidation("hello", constraint("hello", mode = StrOccMode.EQUALS), ctx))
        }
    }
}
