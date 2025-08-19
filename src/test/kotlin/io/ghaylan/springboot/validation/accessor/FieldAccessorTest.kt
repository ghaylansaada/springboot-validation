package io.ghaylan.springboot.validation.accessor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FieldAccessorTest {

    @Test
    fun `should access properties via getter`() {
        // Given
        val accessor = AccessorFactory.build(TestClass::class.java, "propertyWithGetter")
        val instance = TestClass("test value")
        
        // When
        val value = accessor.get(instance)
        
        // Then
        assertEquals("test value", value)
        assertEquals(TestClass::class.java, accessor.containerClass)
    }
    
    @Test
    fun `should access field directly when no getter`() {
        // Given
        val accessor = AccessorFactory.build(TestClass::class.java, "directField")
        val instance = TestClass("test value")
        
        // When
        val value = accessor.get(instance)
        
        // Then
        assertEquals("direct field", value)
    }
    
    @Test
    fun `should access boolean property with is prefix`() {
        // Given
        val accessor = AccessorFactory.build(TestClass::class.java, "active")
        val instance = TestClass("test value")
        
        // When
        val value = accessor.get(instance)
        
        // Then
        assertEquals(true, value)
    }
    
    @Test
    fun `should handle getFromAny with correct type`() {
        // Given
        val accessor = AccessorFactory.build(TestClass::class.java, "propertyWithGetter")
        val instance = TestClass("test value")
        
        // When
        val value = accessor.getFromAny(instance)
        
        // Then
        assertEquals("test value", value)
    }
    
    @Test
    fun `should return null from getFromAny when wrong type and not strict`() {
        // Given
        val accessor = AccessorFactory.build(TestClass::class.java, "propertyWithGetter")
        val wrongInstance = OtherClass()
        
        // When
        val value = accessor.getFromAny(wrongInstance, strict = false)
        
        // Then
        assertNull(value)
    }
    
    @Test
    fun `should throw from getFromAny when wrong type and strict`() {
        // Given
        val accessor = AccessorFactory.build(TestClass::class.java, "propertyWithGetter")
        val wrongInstance = OtherClass()
        
        // Then
        assertThrows(IllegalStateException::class.java) {
            // When
            accessor.getFromAny(wrongInstance, strict = true)
        }
    }
    
    @Test
    fun `should access map entry by key`() {
        // Given
        val accessor = AccessorFactory.build(Map::class.java, "testKey")
        val map = mapOf("testKey" to "map value", "otherKey" to "other value")
        
        // When
        val value = accessor.get(map)
        
        // Then
        assertEquals("map value", value)
    }
    
    // Test classes
    class TestClass(private val value: String) {
        val propertyWithGetter: String
            get() = value
            
        val directField: String = "direct field"
        
        val active: Boolean = true
    }
    
    class OtherClass
}

