package io.ghaylan.springboot.validation.constraints.string

import io.ghaylan.springboot.validation.constraints.string.length.StringLengthConstraint
import io.ghaylan.springboot.validation.constraints.string.length.StringLengthValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class StringLengthValidatorTest {

    private val validator = StringLengthValidator()
    private val context = ValidationContext(
        fieldName = "password",
        fieldPath = "user.password",
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
        val constraint = StringLengthConstraint(min = 5, max = 20, groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when string length is within bounds`() {
        // Given
        val constraint = StringLengthConstraint(min = 5, max = 20, groups = setOf(Any::class))
        val value = "validstring"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when string length equals min bound`() {
        // Given
        val constraint = StringLengthConstraint(min = 5, max = 20, groups = setOf(Any::class))
        val value = "valid"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when string length equals max bound`() {
        // Given
        val constraint = StringLengthConstraint(min = 5, max = 20, groups = setOf(Any::class))
        val value = "validvalidvalidvalid" // 20 chars
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when string length is less than min`() {
        // Given
        val constraint = StringLengthConstraint(min = 5, max = 20, groups = setOf(Any::class))
        val value = "abc" // 3 chars
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("user.password", result?.field)
        assertEquals("STRING_LENGTH_TOO_SHORT", result?.code)
        assertNotNull(result?.data)
        assertEquals(5, result?.data?.get("min"))
        assertEquals(3, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should return ApiError when string length exceeds max`() {
        // Given
        val constraint = StringLengthConstraint(min = 5, max = 20, groups = setOf(Any::class))
        val value = "thisistoolongforthelimithereabcdefghijk" // 38 chars
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("user.password", result?.field)
        assertEquals("STRING_LENGTH_TOO_LONG", result?.code)
        assertNotNull(result?.data)
        assertEquals(20, result?.data?.get("max"))
        assertEquals(38, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = StringLengthConstraint(
            min = 5, 
            max = 20, 
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Password must be between 5 and 20 characters",
                "fr" to "Le mot de passe doit contenir entre 5 et 20 caractères"
            )
        )
        val value = "abc" // 3 chars
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Password must be between 5 and 20 characters", result?.message)
    }
}

