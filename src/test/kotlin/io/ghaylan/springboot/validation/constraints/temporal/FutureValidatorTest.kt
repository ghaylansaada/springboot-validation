package io.ghaylan.springboot.validation.constraints.temporal

import io.ghaylan.springboot.validation.constraints.temporal.future.FutureConstraint
import io.ghaylan.springboot.validation.constraints.temporal.future.FutureValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.*
import java.util.*

class FutureValidatorTest {

    private val validator = FutureValidator()
    private val context = ValidationContext(
        fieldName = "expiryDate",
        fieldPath = "card.expiryDate",
        type = null,
        location = ApiError.ErrorLocation.BODY,
        locale = Locale.ENGLISH,
        language = "en-US",
        stopOnFirstError = true,
        groups = setOf(Any::class),
        array = null,
        containerObject = null
    )
    
    // Mock the clock for testing
    private val fixedClock = Clock.fixed(
        Instant.parse("2023-01-15T10:15:30.00Z"),
        ZoneId.of("UTC")
    )

    @Test
    fun `validate should return null when value is null`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when date is in the future`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val value = LocalDate.now(fixedClock).plusDays(1)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when datetime is in the future`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val value = LocalDateTime.now(fixedClock).plusHours(1)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when instant is in the future`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val value = Instant.now(fixedClock).plusMillis(3600000) // Add 1 hour
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when date is current and inclusive is true`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val value = LocalDate.now(fixedClock)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when date is current and inclusive is false`() {
        // Given
        val constraint = FutureConstraint(inclusive = false, groups = setOf(Any::class))
        val value = LocalDate.now(fixedClock)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("card.expiryDate", result?.field)
        assertEquals("NOT_FUTURE", result?.code)
        assertNotNull(result?.data)
        assertEquals(false, result?.data?.get("inclusive"))
    }
    
    @Test
    fun `validate should return ApiError when date is in the past`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val value = LocalDate.now(fixedClock).minusDays(1)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("NOT_FUTURE", result?.code)
    }
    
    @Test
    fun `validate should handle java Date objects`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = Instant.now(fixedClock).plusMillis(86400000).toEpochMilli() // Add a day
        val value = calendar.time
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = FutureConstraint(
            inclusive = false,
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Date must be in the future",
                "fr" to "La date doit être dans le futur"
            )
        )
        val value = LocalDate.now(fixedClock)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Date must be in the future", result?.message)
    }
    
    @Test
    fun `validate should return ApiError for non-temporal value`() {
        // Given
        val constraint = FutureConstraint(inclusive = true, groups = setOf(Any::class))
        val value = "not a date"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("INVALID_TEMPORAL_TYPE", result?.code)
    }
}

