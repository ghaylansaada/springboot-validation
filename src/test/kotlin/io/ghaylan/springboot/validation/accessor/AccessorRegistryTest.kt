package io.ghaylan.springboot.validation.accessor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccessorRegistryTest {

    @BeforeEach
    fun setUp() {
        // Clear the registry before each test to ensure isolation
        AccessorRegistry.clear()
    }

    @Test
    fun `getOrCreate should create and cache accessor for class field`() {
        // When
        val accessor1 = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        val accessor2 = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        
        // Then
        assertNotNull(accessor1)
        assertSame(accessor1, accessor2, "Should return the same cached accessor instance")
    }
    
    @Test
    fun `getOrCreate should create separate accessors for different fields`() {
        // When
        val accessor1 = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        val accessor2 = AccessorRegistry.getOrCreate(TestClass::class.java, "intField")
        
        // Then
        assertNotNull(accessor1)
        assertNotNull(accessor2)
        assertNotSame(accessor1, accessor2, "Should create different accessor instances for different fields")
    }
    
    @Test
    fun `getOrCreate should create separate accessors for different classes`() {
        // When
        val accessor1 = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        val accessor2 = AccessorRegistry.getOrCreate(OtherClass::class.java, "stringField")
        
        // Then
        assertNotNull(accessor1)
        assertNotNull(accessor2)
        assertNotSame(accessor1, accessor2, "Should create different accessor instances for different classes")
    }
    
    @Test
    fun `accessor should extract field value correctly`() {
        // Given
        val instance = TestClass("test", 123)
        val accessor = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        
        // When
        val value = accessor.get(instance)
        
        // Then
        assertEquals("test", value)
    }
    
    @Test
    fun `clear should remove all cached accessors`() {
        // Given
        AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        
        // When
        AccessorRegistry.clear()
        val accessor1 = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        val accessor2 = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        
        // Then
        assertNotNull(accessor1)
        assertSame(accessor1, accessor2, "After clear, new accessors should be created and cached")
    }
    
    @Test
    fun `getFromAny should return null when instance is null`() {
        // Given
        val accessor = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        
        // When
        val value = accessor.getFromAny(null)
        
        // Then
        assertNull(value)
    }
    
    @Test
    fun `getFromAny should return null when instance is wrong type and not strict`() {
        // Given
        val accessor = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        val instance = OtherClass("other")
        
        // When
        val value = accessor.getFromAny(instance, strict = false)
        
        // Then
        assertNull(value)
    }
    
    @Test
    fun `getFromAny should throw when instance is wrong type and strict`() {
        // Given
        val accessor = AccessorRegistry.getOrCreate(TestClass::class.java, "stringField")
        val instance = OtherClass("other")
        
        // Then
        assertThrows(IllegalStateException::class.java) {
            // When
            accessor.getFromAny(instance, strict = true)
        }
    }
    
    // Test classes
    data class TestClass(
        val stringField: String,
        val intField: Int
    )
    
    data class OtherClass(
        val stringField: String
    )
}

