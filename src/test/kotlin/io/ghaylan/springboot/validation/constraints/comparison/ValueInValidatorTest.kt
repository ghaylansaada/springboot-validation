package io.ghaylan.springboot.validation.constraints.comparison

import io.ghaylan.springboot.validation.constraints.comparison.valuein.ValueInConstraint
import io.ghaylan.springboot.validation.constraints.comparison.valuein.ValueInValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class ValueInValidatorTest {

    private val validator = ValueInValidator()
    private val context = ValidationContext(
        fieldName = "status",
        fieldPath = "order.status",
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
        val constraint = ValueInConstraint(
            values = arrayOf("PENDING", "COMPLETED", "CANCELLED"),
            groups = setOf(Any::class)
        )
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when value is in allowed values`() {
        // Given
        val constraint = ValueInConstraint(
            values = arrayOf("PENDING", "COMPLETED", "CANCELLED"),
            groups = setOf(Any::class)
        )
        val value = "PENDING"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when value is not in allowed values`() {
        // Given
        val constraint = ValueInConstraint(
            values = arrayOf("PENDING", "COMPLETED", "CANCELLED"),
            groups = setOf(Any::class)
        )
        val value = "REJECTED"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("order.status", result?.field)
        assertEquals("VALUE_NOT_ALLOWED", result?.code)
        assertNotNull(result?.data)
        assertEquals(arrayOf("PENDING", "COMPLETED", "CANCELLED").toList(), result?.data?.get("allowedValues"))
        assertEquals("REJECTED", result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should handle integer values`() {
        // Given
        val constraint = ValueInConstraint(
            values = arrayOf("1", "2", "3"),
            groups = setOf(Any::class)
        )
        val value = 2
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle enum values`() {
        // Given
        val constraint = ValueInConstraint(
            values = arrayOf("MONDAY", "TUESDAY", "WEDNESDAY"),
            groups = setOf(Any::class)
        )
        val value = TestDayEnum.MONDAY
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should use case-sensitive comparison for strings`() {
        // Given
        val constraint = ValueInConstraint(
            values = arrayOf("PENDING", "COMPLETED", "CANCELLED"),
            groups = setOf(Any::class)
        )
        val value = "pending" // lowercase
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("VALUE_NOT_ALLOWED", result?.code)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = ValueInConstraint(
            values = arrayOf("PENDING", "COMPLETED", "CANCELLED"),
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Status must be one of: PENDING, COMPLETED, CANCELLED",
                "fr" to "Le statut doit être l'un des suivants : PENDING, COMPLETED, CANCELLED"
            )
        )
        val value = "REJECTED"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Status must be one of: PENDING, COMPLETED, CANCELLED", result?.message)
    }
    
    @Test
    fun `validate should handle boolean values`() {
        // Given
        val constraint = ValueInConstraint(
            values = arrayOf("true", "false"),
            groups = setOf(Any::class)
        )
        val value = true
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    // Test enum
    enum class TestDayEnum {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
    }
}

