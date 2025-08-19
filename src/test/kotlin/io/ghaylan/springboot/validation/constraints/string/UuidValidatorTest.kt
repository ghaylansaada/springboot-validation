package io.ghaylan.springboot.validation.constraints.string

import io.ghaylan.springboot.validation.constraints.string.uuid.UuidConstraint
import io.ghaylan.springboot.validation.constraints.string.uuid.UuidValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale
import java.util.UUID

class UuidValidatorTest {

    private val validator = UuidValidator()
    private val context = ValidationContext(
        fieldName = "id",
        fieldPath = "product.id",
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
        val constraint = UuidConstraint(groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "123e4567-e89b-12d3-a456-426614174000",
        "550e8400-e29b-41d4-a716-446655440000",
        "00000000-0000-0000-0000-000000000000",
        "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    ])
    fun `validate should return null for valid UUIDs`(uuid: String) {
        // Given
        val constraint = UuidConstraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(uuid, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "not-a-uuid",
        "123e4567-e89b-12d3-a456-42661417400", // too short
        "123e4567-e89b-12d3-a456-4266141740000", // too long
        "123e4567-e89b-12d3-a456_426614174000", // invalid character
        "123e4567e89b12d3a456426614174000", // no hyphens
        "123e456-e89b-12d3-a456-426614174000", // wrong segment length
        "123e4567-e89-12d3-a456-426614174000", // wrong segment length
        "123e4567-e89b-12d-a456-426614174000", // wrong segment length
        "123e4567-e89b-12d3-a45-426614174000", // wrong segment length
        "123e4567-e89b-12d3-a456-42661417400", // wrong segment length
        "{123e4567-e89b-12d3-a456-426614174000}", // wrapped in braces
        "gggggggg-gggg-gggg-gggg-gggggggggggg" // invalid hex characters
    ])
    fun `validate should return ApiError for invalid UUIDs`(uuid: String) {
        // Given
        val constraint = UuidConstraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(uuid, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("product.id", result?.field)
        assertEquals("UUID_INVALID", result?.code)
        assertNotNull(result?.data)
        assertEquals(uuid, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should handle UUID objects`() {
        // Given
        val constraint = UuidConstraint(groups = setOf(Any::class))
        val uuidObj = UUID.randomUUID()
        
        // When
        val result = validator.validate(uuidObj, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle versions when specified`() {
        // Given
        val constraint = UuidConstraint(version = 4, groups = setOf(Any::class))
        // UUID version 4 (random)
        val validUuid = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        // UUID version 1 (time-based)
        val invalidUuid = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
        
        // When
        val validResult = validator.validate(validUuid, constraint, context)
        val invalidResult = validator.validate(invalidUuid, constraint, context)
        
        // Then
        assertNull(validResult)
        assertNotNull(invalidResult)
        assertEquals("UUID_INVALID_VERSION", invalidResult?.code)
        assertEquals(4, invalidResult?.data?.get("requiredVersion"))
        assertEquals(1, invalidResult?.data?.get("actualVersion"))
    }
    
    @Test
    fun `validate should handle uppercase UUIDs`() {
        // Given
        val constraint = UuidConstraint(groups = setOf(Any::class))
        val uuid = "123E4567-E89B-12D3-A456-426614174000" // uppercase
        
        // When
        val result = validator.validate(uuid, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = UuidConstraint(
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Must be a valid UUID",
                "fr" to "Doit être un UUID valide"
            )
        )
        val value = "not-a-uuid"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Must be a valid UUID", result?.message)
    }
    
    @Test
    fun `validate should handle empty string`() {
        // Given
        val constraint = UuidConstraint(groups = setOf(Any::class))
        val value = ""
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("UUID_INVALID", result?.code)
    }
}

