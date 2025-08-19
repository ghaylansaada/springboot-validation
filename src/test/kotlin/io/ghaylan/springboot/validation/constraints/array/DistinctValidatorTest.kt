package io.ghaylan.springboot.validation.constraints.array

import io.ghaylan.springboot.validation.constraints.array.distinct.DistinctConstraint
import io.ghaylan.springboot.validation.constraints.array.distinct.DistinctValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class DistinctValidatorTest {

    private val validator = DistinctValidator()
    private val context = ValidationContext(
        fieldName = "tags",
        fieldPath = "post.tags",
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
        val constraint = DistinctConstraint(by = emptyArray(), groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when collection has distinct elements`() {
        // Given
        val constraint = DistinctConstraint(by = emptyArray(), groups = setOf(Any::class))
        val value = listOf("tag1", "tag2", "tag3")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when collection has duplicate elements`() {
        // Given
        val constraint = DistinctConstraint(by = emptyArray(), groups = setOf(Any::class))
        val value = listOf("tag1", "tag2", "tag1", "tag3")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("post.tags", result?.field)
        assertEquals("NOT_DISTINCT", result?.code)
        assertNotNull(result?.data)
        assertEquals("tag1", result?.data?.get("duplicateValue"))
    }
    
    @Test
    fun `validate should return null when object collection has distinct elements by field`() {
        // Given
        val constraint = DistinctConstraint(by = arrayOf("id"), groups = setOf(Any::class))
        val value = listOf(
            TestItem(1, "First"),
            TestItem(2, "Second"),
            TestItem(3, "Third")
        )
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when object collection has duplicates by field`() {
        // Given
        val constraint = DistinctConstraint(by = arrayOf("id"), groups = setOf(Any::class))
        val value = listOf(
            TestItem(1, "First"),
            TestItem(2, "Second"),
            TestItem(1, "Another First") // Duplicate ID
        )
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("post.tags", result?.field)
        assertEquals("NOT_DISTINCT", result?.code)
        assertNotNull(result?.data)
        assertEquals("id", result?.data?.get("duplicateField"))
    }
    
    @Test
    fun `validate should handle multiple fields for distinction check`() {
        // Given
        val constraint = DistinctConstraint(by = arrayOf("id", "name"), groups = setOf(Any::class))
        val value = listOf(
            TestItem(1, "First"),
            TestItem(1, "Second"), // Same ID but different name, so it's ok
            TestItem(2, "Second")  // Same name but different ID, so it's ok
        )
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should detect duplicates with multiple fields`() {
        // Given
        val constraint = DistinctConstraint(by = arrayOf("id", "name"), groups = setOf(Any::class))
        val value = listOf(
            TestItem(1, "First"),
            TestItem(2, "Second"),
            TestItem(1, "First") // Duplicate combination of ID and name
        )
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("NOT_DISTINCT", result?.code)
        assertNotNull(result?.data)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = DistinctConstraint(
            by = emptyArray(),
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Tags must be unique",
                "fr" to "Les tags doivent être uniques"
            )
        )
        val value = listOf("tag1", "tag2", "tag1")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Tags must be unique", result?.message)
    }
    
    // Helper data class for testing
    data class TestItem(val id: Int, val name: String)
}

