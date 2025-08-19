package io.ghaylan.springboot.validation.constraints.string

import io.ghaylan.springboot.validation.constraints.string.contains.StrOccConstraint
import io.ghaylan.springboot.validation.constraints.string.contains.StrOccValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class StrOccValidatorTest {

    private val validator = StrOccValidator()
    private val context = ValidationContext(
        fieldName = "content",
        fieldPath = "article.content",
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
        val constraint = StrOccConstraint(
            substring = "test",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when string contains substring`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "test",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = "This is a test string"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when string does not contain substring`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "missing",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = "This is a test string"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("article.content", result?.field)
        assertEquals("STRING_MUST_CONTAIN", result?.code)
        assertNotNull(result?.data)
        assertEquals("missing", result?.data?.get("substring"))
        assertEquals("This is a test string", result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should respect ignoreCase when true`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "TEST",
            ignoreCase = true,
            groups = setOf(Any::class)
        )
        val value = "This is a test string"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should respect ignoreCase when false`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "TEST",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = "This is a test string"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("STRING_MUST_CONTAIN", result?.code)
    }
    
    @Test
    fun `validate should handle empty substring`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = "Any string"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle empty string`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "test",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = ""
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("STRING_MUST_CONTAIN", result?.code)
    }
    
    @Test
    fun `validate should handle special characters`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "#$%^",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = "String with #$%^ special chars"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should convert non-string values to string`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "123",
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value = 12345
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "required-text",
            ignoreCase = false,
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Content must contain 'required-text'",
                "fr" to "Le contenu doit contenir 'required-text'"
            )
        )
        val value = "This text doesn't have the required substring"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Content must contain 'required-text'", result?.message)
    }
    
    @Test
    fun `validate should handle occurrence count when min is specified`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "test",
            min = 2,
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value1 = "This test string has one test occurrence"
        val value2 = "This test only appears once"
        
        // When
        val result1 = validator.validate(value1, constraint, context)
        val result2 = validator.validate(value2, constraint, context)
        
        // Then
        assertNull(result1)
        assertNotNull(result2)
        assertEquals("STRING_INSUFFICIENT_OCCURRENCES", result2?.code)
        assertEquals(2, result2?.data?.get("min"))
        assertEquals(1, result2?.data?.get("actual"))
    }
    
    @Test
    fun `validate should handle occurrence count when max is specified`() {
        // Given
        val constraint = StrOccConstraint(
            substring = "test",
            max = 1,
            ignoreCase = false,
            groups = setOf(Any::class)
        )
        val value1 = "This test only appears once"
        val value2 = "This test string has one test occurrence"
        
        // When
        val result1 = validator.validate(value1, constraint, context)
        val result2 = validator.validate(value2, constraint, context)
        
        // Then
        assertNull(result1)
        assertNotNull(result2)
        assertEquals("STRING_EXCESSIVE_OCCURRENCES", result2?.code)
        assertEquals(1, result2?.data?.get("max"))
        assertEquals(2, result2?.data?.get("actual"))
    }
}

