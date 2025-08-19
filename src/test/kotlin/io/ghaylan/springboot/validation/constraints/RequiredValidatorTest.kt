package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.constraints.required.RequiredConstraint
import io.ghaylan.springboot.validation.constraints.required.RequiredValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class RequiredValidatorTest {

    private val validator = RequiredValidator()
    private val constraint = RequiredConstraint(
        groups = setOf(Any::class)
    )
    
    private val context = ValidationContext(
        fieldName = "testField",
        fieldPath = "user.testField",
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
    fun `validate should return null when value is not null`() {
        // Given
        val value = "test"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when value is null`() {
        // Given
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals(ApiError.ErrorLocation.BODY, result?.location)
        assertEquals("user.testField", result?.field)
        assertEquals("REQUIRED", result?.code)
    }
    
    @Test
    fun `validate should return ApiError when string is empty`() {
        // Given
        val value = ""
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("user.testField", result?.field)
        assertEquals("REQUIRED", result?.code)
    }
    
    @Test
    fun `validate should return ApiError when collection is empty`() {
        // Given
        val value = emptyList<String>()
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("user.testField", result?.field)
        assertEquals("REQUIRED", result?.code)
    }
    
    @Test
    fun `validate should return null when collection has items`() {
        // Given
        val value = listOf("item")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when map is empty`() {
        // Given
        val value = emptyMap<String, String>()
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("user.testField", result?.field)
        assertEquals("REQUIRED", result?.code)
    }
    
    @Test
    fun `validate should return null when map has entries`() {
        // Given
        val value = mapOf("key" to "value")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
}

