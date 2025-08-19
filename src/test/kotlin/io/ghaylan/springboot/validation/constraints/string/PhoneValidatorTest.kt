package io.ghaylan.springboot.validation.constraints.string

import io.ghaylan.springboot.validation.constraints.string.phone.PhoneConstraint
import io.ghaylan.springboot.validation.constraints.string.phone.PhoneValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class PhoneValidatorTest {

    private val validator = PhoneValidator()
    private val context = ValidationContext(
        fieldName = "phone",
        fieldPath = "user.phone",
        type = null,
        location = ApiError.ErrorLocation.BODY,
        locale = Locale.ENGLISH,
        language = "en-US",
        stopOnFirstError = true,
        groups = setOf(Any::class),
        array = null,
        containerObject = null
    )

    @Test
    fun `validate should return null when value is null`() {
        // Given
        val constraint = PhoneConstraint(groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "+1-202-555-0123",
        "+44 20 7946 0958",
        "+33 1 42 68 53 00",
        "+81 3-1234-5678",
        "+86 10 6552 9988",
        "+49 30 901820",
        "+61 2 9374 2222",
        "123-456-7890",
        "(123) 456-7890",
        "123 456 7890",
        "123.456.7890",
        "1234567890"
    ])
    fun `validate should return null for valid phone numbers`(phone: String) {
        // Given
        val constraint = PhoneConstraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(phone, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "not-a-phone",
        "123",
        "12",
        "+",
        "+12",
        "++1-123-456-7890",
        "1-23-45",
        "1 2 3 4",
        "1+2+3+4",
        "phone: 123-456-7890",
        "+1@123-456-7890"
    ])
    fun `validate should return ApiError for invalid phone numbers`(phone: String) {
        // Given
        val constraint = PhoneConstraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(phone, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("user.phone", result?.field)
        assertEquals("PHONE_INVALID", result?.code)
        assertNotNull(result?.data)
        assertEquals(phone, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should handle country restrictions`() {
        // Given
        val constraint = PhoneConstraint(
            countries = arrayOf("US", "CA"),
            groups = setOf(Any::class)
        )
        val usPhone = "+1 202-555-0123"     // US
        val caPhone = "+1 416-555-0123"     // Canada
        val ukPhone = "+44 20 7946 0958"    // UK
        
        // When
        val usResult = validator.validate(usPhone, constraint, context)
        val caResult = validator.validate(caPhone, constraint, context)
        val ukResult = validator.validate(ukPhone, constraint, context)
        
        // Then
        assertNull(usResult)
        assertNull(caResult)
        assertNotNull(ukResult)
        assertEquals("PHONE_INVALID_COUNTRY", ukResult?.code)
        assertEquals(arrayOf("US", "CA").toList(), ukResult?.data?.get("allowedCountries"))
    }
    
    @Test
    fun `validate should support optional country code`() {
        // Given
        val constraint = PhoneConstraint(
            requireCountryCode = false,
            groups = setOf(Any::class)
        )
        // No country code
        val value = "555-123-4567"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should enforce required country code when set`() {
        // Given
        val constraint = PhoneConstraint(
            requireCountryCode = true,
            groups = setOf(Any::class)
        )
        // No country code
        val value = "555-123-4567"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("PHONE_MISSING_COUNTRY_CODE", result?.code)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = PhoneConstraint(
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Please enter a valid phone number",
                "fr" to "Veuillez saisir un numéro de téléphone valide"
            )
        )
        val value = "invalid-phone"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Please enter a valid phone number", result?.message)
    }
    
    @Test
    fun `validate should handle empty string`() {
        // Given
        val constraint = PhoneConstraint(groups = setOf(Any::class))
        val value = ""
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("PHONE_INVALID", result?.code)
    }
    
    @Test
    fun `validate should handle whitespace-only string`() {
        // Given
        val constraint = PhoneConstraint(groups = setOf(Any::class))
        val value = "   "
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("PHONE_INVALID", result?.code)
    }
}

