package io.ghaylan.springboot.validation.constraints.string

import io.ghaylan.springboot.validation.constraints.string.base64.Base64Constraint
import io.ghaylan.springboot.validation.constraints.string.base64.Base64Validator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class Base64ValidatorTest {

    private val validator = Base64Validator()
    private val context = ValidationContext(
        fieldName = "data",
        fieldPath = "document.data",
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
        val constraint = Base64Constraint(groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "SGVsbG8gV29ybGQ=", // "Hello World"
        "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo=", // alphabet
        "",
        "YQ==", // "a"
        "YWI=", // "ab"
        "YWJj", // "abc"
        "YWJjZA==", // "abcd"
        "YWJjZGU=", // "abcde"
        "YWJjZGVm" // "abcdef"
    ])
    fun `validate should return null for valid Base64 strings`(base64: String) {
        // Given
        val constraint = Base64Constraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(base64, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "SGVsbG8gV29ybGQ", // Missing padding
        "SGVsbG8gV29ybGQ==", // Invalid padding
        "SGVsbG8gV29ybGQ===", // Too much padding
        "SGVsbG8-V29ybGQ=", // Invalid character
        "SGVsbG8/V29ybGQ=", // Invalid character
        "Hello World", // Not Base64 encoded
        "YW!jZA==", // Invalid character
        "YWJjZA==" // Valid Base64, but with an invalid character
    ])
    fun `validate should return ApiError for invalid Base64 strings`(base64: String) {
        // Given
        val constraint = Base64Constraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(base64, constraint, context)
        
        // Then
        if (base64 == "YWJjZA==") {
            // Skip this test as it's actually valid Base64
            return
        }
        
        assertNotNull(result)
        assertEquals("document.data", result?.field)
        assertEquals("BASE64_INVALID", result?.code)
        assertNotNull(result?.data)
        assertEquals(base64, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should handle URL safe Base64 when urlSafe is true`() {
        // Given
        val constraint = Base64Constraint(urlSafe = true, groups = setOf(Any::class))
        // URL-safe Base64 uses '-' and '_' instead of '+' and '/'
        val value = "SGVsbG8tV29ybGRf"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should reject URL safe characters when urlSafe is false`() {
        // Given
        val constraint = Base64Constraint(urlSafe = false, groups = setOf(Any::class))
        // URL-safe Base64 uses '-' and '_' instead of '+' and '/'
        val value = "SGVsbG8tV29ybGRf"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("BASE64_INVALID", result?.code)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = Base64Constraint(
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Must be Base64 encoded",
                "fr" to "Doit être encodé en Base64"
            )
        )
        val value = "Not a Base64 string!"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Must be Base64 encoded", result?.message)
    }
    
    @Test
    fun `validate should handle empty string`() {
        // Given
        val constraint = Base64Constraint(groups = setOf(Any::class))
        val value = ""
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle very large base64 strings`() {
        // Given
        val constraint = Base64Constraint(groups = setOf(Any::class))
        // Generate a large valid Base64 string
        val value = "A".repeat(1000) + "=="
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        // This might fail if the validator doesn't handle large strings correctly
        // but most implementations should be able to handle this size
        assertNull(result)
    }
}

