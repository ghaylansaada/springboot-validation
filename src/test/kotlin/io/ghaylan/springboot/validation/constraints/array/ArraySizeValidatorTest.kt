package io.ghaylan.springboot.validation.constraints.array

import io.ghaylan.springboot.validation.constraints.array.size.ArraySizeConstraint
import io.ghaylan.springboot.validation.constraints.array.size.ArraySizeValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class ArraySizeValidatorTest {

    private val validator = ArraySizeValidator()
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
        val constraint = ArraySizeConstraint(min = 1, max = 10, groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when list size is within bounds`() {
        // Given
        val constraint = ArraySizeConstraint(min = 1, max = 10, groups = setOf(Any::class))
        val value = listOf("tag1", "tag2", "tag3")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when array size is within bounds`() {
        // Given
        val constraint = ArraySizeConstraint(min = 1, max = 10, groups = setOf(Any::class))
        val value = arrayOf("tag1", "tag2", "tag3")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when set size is within bounds`() {
        // Given
        val constraint = ArraySizeConstraint(min = 1, max = 10, groups = setOf(Any::class))
        val value = setOf("tag1", "tag2", "tag3")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when collection size equals min bound`() {
        // Given
        val constraint = ArraySizeConstraint(min = 2, max = 10, groups = setOf(Any::class))
        val value = listOf("tag1", "tag2")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return null when collection size equals max bound`() {
        // Given
        val constraint = ArraySizeConstraint(min = 1, max = 3, groups = setOf(Any::class))
        val value = listOf("tag1", "tag2", "tag3")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when collection size is less than min`() {
        // Given
        val constraint = ArraySizeConstraint(min = 3, max = 10, groups = setOf(Any::class))
        val value = listOf("tag1", "tag2")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("post.tags", result?.field)
        assertEquals("ARRAY_SIZE_TOO_SMALL", result?.code)
        assertNotNull(result?.data)
        assertEquals(3, result?.data?.get("min"))
        assertEquals(2, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should return ApiError when collection size exceeds max`() {
        // Given
        val constraint = ArraySizeConstraint(min = 1, max = 3, groups = setOf(Any::class))
        val value = listOf("tag1", "tag2", "tag3", "tag4", "tag5")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("post.tags", result?.field)
        assertEquals("ARRAY_SIZE_TOO_LARGE", result?.code)
        assertNotNull(result?.data)
        assertEquals(3, result?.data?.get("max"))
        assertEquals(5, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = ArraySizeConstraint(
            min = 1,
            max = 3,
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "You can only provide between 1 and 3 tags",
                "fr" to "Vous pouvez fournir entre 1 et 3 tags seulement"
            )
        )
        val value = listOf("tag1", "tag2", "tag3", "tag4", "tag5")
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("You can only provide between 1 and 3 tags", result?.message)
    }
    
    @Test
    fun `validate should handle empty collection`() {
        // Given
        val constraint = ArraySizeConstraint(min = 1, max = 10, groups = setOf(Any::class))
        val value = emptyList<String>()
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("ARRAY_SIZE_TOO_SMALL", result?.code)
    }
}

