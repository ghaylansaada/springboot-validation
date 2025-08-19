package io.ghaylan.springboot.validation.model.errors

import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class ApiErrorCollectorTest {

    @Test
    fun `body should add error with BODY location`() {
        // Given
        val collector = ApiErrorCollector()
        
        // When
        collector.body {
            field("email")
            code("REQUIRED")
            data(mapOf("actualValue" to ""))
        }
        
        // Then
        val errors = collector.all()
        assertEquals(1, errors.size)
        assertEquals("email", errors[0].field)
        assertEquals("REQUIRED", errors[0].code)
        assertEquals(ApiError.ErrorLocation.BODY, errors[0].location)
    }
    
    @Test
    fun `query should add error with QUERY location`() {
        // Given
        val collector = ApiErrorCollector()
        
        // When
        collector.query {
            field("page")
            code("INVALID_NUMBER")
        }
        
        // Then
        val errors = collector.all()
        assertEquals(1, errors.size)
        assertEquals("page", errors[0].field)
        assertEquals("INVALID_NUMBER", errors[0].code)
        assertEquals(ApiError.ErrorLocation.QUERY, errors[0].location)
    }
    
    @Test
    fun `header should add error with HEADER location`() {
        // Given
        val collector = ApiErrorCollector()
        
        // When
        collector.header {
            field("Authorization")
            code("MISSING")
        }
        
        // Then
        val errors = collector.all()
        assertEquals(1, errors.size)
        assertEquals("Authorization", errors[0].field)
        assertEquals("MISSING", errors[0].code)
        assertEquals(ApiError.ErrorLocation.HEADER, errors[0].location)
    }
    
    @Test
    fun `path should add error with PATH location`() {
        // Given
        val collector = ApiErrorCollector()
        
        // When
        collector.path {
            field("id")
            code("INVALID_FORMAT")
        }
        
        // Then
        val errors = collector.all()
        assertEquals(1, errors.size)
        assertEquals("id", errors[0].field)
        assertEquals("INVALID_FORMAT", errors[0].code)
        assertEquals(ApiError.ErrorLocation.PATH, errors[0].location)
    }
    
    @Test
    fun `business should add error with BUSINESS location`() {
        // Given
        val collector = ApiErrorCollector()
        
        // When
        collector.business {
            field("order")
            code("INSUFFICIENT_INVENTORY")
        }
        
        // Then
        val errors = collector.all()
        assertEquals(1, errors.size)
        assertEquals("order", errors[0].field)
        assertEquals("INSUFFICIENT_INVENTORY", errors[0].code)
        assertEquals(ApiError.ErrorLocation.BUSINESS, errors[0].location)
    }
    
    @Test
    fun `should resolve messages based on locale`() {
        // Given
        val collector = ApiErrorCollector()
        collector.body {
            field("email")
            code("REQUIRED")
            messages {
                add("en", "Email is required")
                add("fr", "L'email est requis")
                add("en-US", "Email is required (US)")
            }
        }
        
        // When/Then - English (US)
        val exception1 = assertThrows(ConstraintViolationException::class.java) {
            collector.throwIfNotEmpty(Locale("en", "US"))
        }
        assertEquals("Email is required (US)", exception1.errors[0].message)
        
        // Given - Create a new collector since throwIfNotEmpty clears messages
        val collector2 = ApiErrorCollector()
        collector2.body {
            field("email")
            code("REQUIRED")
            messages {
                add("en", "Email is required")
                add("fr", "L'email est requis")
            }
        }
        
        // When/Then - French
        val exception2 = assertThrows(ConstraintViolationException::class.java) {
            collector2.throwIfNotEmpty(Locale.FRENCH)
        }
        assertEquals("L'email est requis", exception2.errors[0].message)
        
        // Given - Create a new collector for German locale test
        val collector3 = ApiErrorCollector()
        collector3.body {
            field("email")
            code("REQUIRED")
            messages {
                add("en", "Email is required")
                add("fr", "L'email est requis")
            }
        }
        
        // When/Then - German (fallback to English)
        val exception3 = assertThrows(ConstraintViolationException::class.java) {
            collector3.throwIfNotEmpty(Locale.GERMAN)
        }
        assertEquals("Email is required", exception3.errors[0].message)
    }
    
    @Test
    fun `throwIfNotEmpty should do nothing when no errors`() {
        // Given
        val collector = ApiErrorCollector()
        
        // When/Then - Should not throw
        collector.throwIfNotEmpty(Locale.ENGLISH)
    }
    
    @Test
    fun `all should return all collected errors`() {
        // Given
        val collector = ApiErrorCollector()
        collector.body {
            field("email")
            code("REQUIRED")
        }
        collector.query {
            field("page")
            code("INVALID_NUMBER")
        }
        
        // When
        val errors = collector.all()
        
        // Then
        assertEquals(2, errors.size)
    }
}

