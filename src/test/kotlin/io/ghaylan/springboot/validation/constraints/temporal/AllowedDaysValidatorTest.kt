package io.ghaylan.springboot.validation.constraints.temporal

import io.ghaylan.springboot.validation.constraints.temporal.alloweddays.AllowedDaysConstraint
import io.ghaylan.springboot.validation.constraints.temporal.alloweddays.AllowedDaysValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.*
import java.util.*

class AllowedDaysValidatorTest {

    private val validator = AllowedDaysValidator()
    private val context = ValidationContext(
        fieldName = "deliveryDate",
        fieldPath = "order.deliveryDate",
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
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name, DayOfWeek.FRIDAY.name),
            groups = setOf(Any::class)
        )
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when date is on allowed day`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name, DayOfWeek.FRIDAY.name),
            groups = setOf(Any::class)
        )
        // 2023-01-16 was a Monday
        val value = LocalDate.of(2023, 1, 16)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when date is not on allowed day`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name, DayOfWeek.FRIDAY.name),
            groups = setOf(Any::class)
        )
        // 2023-01-17 was a Tuesday
        val value = LocalDate.of(2023, 1, 17)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("order.deliveryDate", result?.field)
        assertEquals("DAY_NOT_ALLOWED", result?.code)
        assertNotNull(result?.data)
        assertEquals(arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name, DayOfWeek.FRIDAY.name).toList(), 
                     result?.data?.get("allowedDays"))
        assertEquals(DayOfWeek.TUESDAY.name, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should handle LocalDateTime values`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name),
            groups = setOf(Any::class)
        )
        // 2023-01-16 was a Monday
        val value = LocalDateTime.of(2023, 1, 16, 10, 30, 0)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle ZonedDateTime values`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name),
            groups = setOf(Any::class)
        )
        // 2023-01-16 was a Monday
        val value = ZonedDateTime.of(2023, 1, 16, 10, 30, 0, 0, ZoneId.of("UTC"))
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle Instant values`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name),
            groups = setOf(Any::class)
        )
        // Create an Instant that corresponds to Monday, January 16, 2023
        val value = LocalDate.of(2023, 1, 16).atStartOfDay(ZoneId.of("UTC")).toInstant()
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle java Date objects`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name),
            groups = setOf(Any::class)
        )
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.JANUARY, 16) // Monday, January 16, 2023
        val value = calendar.time
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle case-insensitive day names`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf("monday", "wednesday"), // lowercase
            groups = setOf(Any::class)
        )
        // 2023-01-16 was a Monday
        val value = LocalDate.of(2023, 1, 16)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name, DayOfWeek.WEDNESDAY.name),
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Delivery is only available on Monday and Wednesday",
                "fr" to "La livraison n'est disponible que le lundi et le mercredi"
            )
        )
        // 2023-01-17 was a Tuesday
        val value = LocalDate.of(2023, 1, 17)
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Delivery is only available on Monday and Wednesday", result?.message)
    }
    
    @Test
    fun `validate should return ApiError for non-temporal value`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf(DayOfWeek.MONDAY.name),
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
    fun `validate should return ApiError for invalid day name`() {
        // Given
        val constraint = AllowedDaysConstraint(
            allowedDays = arrayOf("INVALID_DAY", DayOfWeek.MONDAY.name),
            groups = setOf(Any::class)
        )
        val value = LocalDate.of(2023, 1, 17) // Tuesday
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("DAY_NOT_ALLOWED", result?.code)
    }
}

