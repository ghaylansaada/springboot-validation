package io.ghaylan.springboot.validation.constraints.string

import io.ghaylan.springboot.validation.constraints.string.regex.RegexConstraint
import io.ghaylan.springboot.validation.constraints.string.regex.RegexValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class RegexValidatorTest {

    private val validator = RegexValidator()
    private val context = ValidationContext(
        fieldName = "zipCode",
        fieldPath = "address.zipCode",
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
        val constraint = RegexConstraint(pattern = "^\\d{5}$", groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when value matches regex pattern`() {
        // Given
        val constraint = RegexConstraint(pattern = "^\\d{5}$", groups = setOf(Any::class))
        val value = "12345"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when value does not match regex pattern`() {
        // Given
        val constraint = RegexConstraint(pattern = "^\\d{5}$", groups = setOf(Any::class))
        val value = "1234"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("address.zipCode", result?.field)
        assertEquals("REGEX_PATTERN_MISMATCH", result?.code)
        assertNotNull(result?.data)
        assertEquals("^\\d{5}$", result?.data?.get("pattern"))
        assertEquals("1234", result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should return ApiError for non-string value`() {
        // Given
        val constraint = RegexConstraint(pattern = "^\\d{5}$", groups = setOf(Any::class))
        val value = 12345 // Integer instead of String
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("REGEX_PATTERN_MISMATCH", result?.code)
    }
    
    @Test
    fun `validate should handle complex regex patterns`() {
        // Given
        val constraint = RegexConstraint(
            pattern = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$",
            groups = setOf(Any::class)
        )
        val value = "test@example.com"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = RegexConstraint(
            pattern = "^\\d{5}$",
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "ZIP code must be exactly 5 digits",
                "fr" to "Le code postal doit comporter exactement 5 chiffres"
            )
        )
        val value = "1234a"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("ZIP code must be exactly 5 digits", result?.message)
    }
    
    @Test
    fun `validate should handle special regex characters`() {
        // Given
        val constraint = RegexConstraint(pattern = "^[$€£]\\d+\\.\\d{2}$", groups = setOf(Any::class))
        val value = "$99.99"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError for empty string when pattern requires content`() {
        // Given
        val constraint = RegexConstraint(pattern = "^\\d+$", groups = setOf(Any::class))
        val value = ""
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("REGEX_PATTERN_MISMATCH", result?.code)
    }
}

