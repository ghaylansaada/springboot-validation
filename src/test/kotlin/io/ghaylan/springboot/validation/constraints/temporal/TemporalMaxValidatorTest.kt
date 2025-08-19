package io.ghaylan.springboot.validation.constraints.temporal

import io.ghaylan.springboot.validation.constraints.temporal.max.TemporalMaxConstraint
import io.ghaylan.springboot.validation.constraints.temporal.max.TemporalMaxValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.*
import java.util.*

class TemporalMaxValidatorTest {

    private val validator = TemporalMaxValidator()
    private val context = ValidationContext(
        fieldName = "eventDate",
        fieldPath = "event.eventDate",
        type = null,
        location = ApiError.ErrorLocation.BODY,
        locale = Locale.ENGLISH,
        language = "en-US",
        stopOnFirstError = true,
        groups = setOf(Any::class),
        array = null,
        containerObject = null
    )

    // Reference date for tests: January 15, 2023
    private val referenceDateString = "2023-01-15"
    private val referenceInstantString = "2023-01-15T00:00:00Z"

    @Test
    fun `validate should return null when value is null`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceDateString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when date is before max`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceDateString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = LocalDate.parse("2023-01-10")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when date equals max and inclusive is true`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceDateString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = LocalDate.parse(referenceDateString)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when date equals max and inclusive is false`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceDateString,
            inclusive = false,
            groups = setOf(Any::class)
        )
        val value = LocalDate.parse(referenceDateString)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("event.eventDate", result?.field)
        assertEquals("TEMPORAL_TOO_LATE", result?.code)
        assertNotNull(result?.data)
        assertEquals(referenceDateString, result?.data?.get("max"))
        assertEquals(false, result?.data?.get("inclusive"))
    }
    
    @Test
    fun `validate should return ApiError when date is after max`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceDateString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = LocalDate.parse("2023-01-20")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("TEMPORAL_TOO_LATE", result?.code)
    }
    
    @Test
    fun `validate should handle LocalDateTime values`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceInstantString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = LocalDateTime.parse("2023-01-10T15:30:00")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle Instant values`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceInstantString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = Instant.parse("2023-01-10T15:30:00Z")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle ZonedDateTime values`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceInstantString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = ZonedDateTime.parse("2023-01-10T15:30:00+01:00")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle java Date objects`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceInstantString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.JANUARY, 10) // Month is 0-based in Calendar
        val value = calendar.time
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceDateString,
            inclusive = true,
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Event date must not be after January 15, 2023",
                "fr" to "La date de l'événement ne doit pas être après le 15 janvier 2023"
            )
        )
        val value = LocalDate.parse("2023-01-20")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Event date must not be after January 15, 2023", result?.message)
    }
    
    @Test
    fun `validate should return ApiError for non-temporal value`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = referenceDateString,
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = "not a date"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("INVALID_TEMPORAL_TYPE", result?.code)
    }
    
    @Test
    fun `validate should handle different temporal type formats`() {
        // Given
        val constraint = TemporalMaxConstraint(
            max = "2023-01-15T10:15:30+01:00", // ZonedDateTime format
            inclusive = true,
            groups = setOf(Any::class)
        )
        val value = LocalDate.parse("2023-01-14")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
}

