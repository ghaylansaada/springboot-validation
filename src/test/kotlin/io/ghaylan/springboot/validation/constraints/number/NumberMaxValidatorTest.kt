package io.ghaylan.springboot.validation.constraints.number

import io.ghaylan.springboot.validation.constraints.number.max.NumberMaxConstraint
import io.ghaylan.springboot.validation.constraints.number.max.NumberMaxValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Locale

class NumberMaxValidatorTest {

    private val validator = NumberMaxValidator()
    private val context = ValidationContext(
        fieldName = "price",
        fieldPath = "product.price",
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
        val constraint = NumberMaxConstraint(max = "100", inclusive = true, groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when integer value is less than max`() {
        // Given
        val constraint = NumberMaxConstraint(max = "100", inclusive = true, groups = setOf(Any::class))
        val value = 50
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when double value is less than max`() {
        // Given
        val constraint = NumberMaxConstraint(max = "100.5", inclusive = true, groups = setOf(Any::class))
        val value = 99.9
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when BigDecimal value is less than max`() {
        // Given
        val constraint = NumberMaxConstraint(max = "100.5", inclusive = true, groups = setOf(Any::class))
        val value = BigDecimal("100.499")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when value equals max and inclusive is true`() {
        // Given
        val constraint = NumberMaxConstraint(max = "100", inclusive = true, groups = setOf(Any::class))
        val value = 100
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when value equals max and inclusive is false`() {
        // Given
        val constraint = NumberMaxConstraint(max = "100", inclusive = false, groups = setOf(Any::class))
        val value = 100
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("product.price", result?.field)
        assertEquals("NUMBER_TOO_LARGE", result?.code)
        assertNotNull(result?.data)
        assertEquals("100", result?.data?.get("max"))
        assertEquals(false, result?.data?.get("inclusive"))
        assertEquals(100, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should return ApiError when value exceeds max`() {
        // Given
        val constraint = NumberMaxConstraint(max = "100", inclusive = true, groups = setOf(Any::class))
        val value = 150
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("product.price", result?.field)
        assertEquals("NUMBER_TOO_LARGE", result?.code)
        assertNotNull(result?.data)
        assertEquals("100", result?.data?.get("max"))
        assertEquals(true, result?.data?.get("inclusive"))
        assertEquals(150, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = NumberMaxConstraint(
            max = "100",
            inclusive = true,
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Price cannot exceed \$100",
                "fr" to "Le prix ne peut pas dépasser 100€"
            )
        )
        val value = 150
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Price cannot exceed \$100", result?.message)
    }
    
    @Test
    fun `validate should handle string representations of numbers`() {
        // Given
        val constraint = NumberMaxConstraint(max = "100", inclusive = true, groups = setOf(Any::class))
        val value = "150"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("NUMBER_TOO_LARGE", result?.code)
    }
}

