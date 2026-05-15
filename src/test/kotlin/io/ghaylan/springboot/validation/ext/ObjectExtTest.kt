package io.ghaylan.springboot.validation.ext

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class ObjectExtTest {

    @Test
    fun `Unit is deep null or empty`() {
        assertTrue(Unit.isDeepNullOrEmpty())
    }

    @Test
    fun `empty Optional is deep null or empty`() {
        assertTrue(Optional.empty<String>().isDeepNullOrEmpty())
    }

    @Test
    fun `present Optional is not deep null or empty`() {
        assertFalse(Optional.of("value").isDeepNullOrEmpty())
    }

    @Test
    fun `blank string is deep null or empty`() {
        assertTrue("".isDeepNullOrEmpty())
        assertTrue("   ".isDeepNullOrEmpty())
    }

    @Test
    fun `non-blank string is not deep null or empty`() {
        assertFalse("hello".isDeepNullOrEmpty())
    }

    @Test
    fun `whitespace char is deep null or empty`() {
        assertTrue(' '.isDeepNullOrEmpty())
        assertTrue('\u0000'.isDeepNullOrEmpty())
    }

    @Test
    fun `non-whitespace char is not deep null or empty`() {
        assertFalse('a'.isDeepNullOrEmpty())
    }

    @Test
    fun `empty collection is deep null or empty`() {
        assertTrue(emptyList<String>().isDeepNullOrEmpty())
    }

    @Test
    fun `collection with all nulls is deep null or empty`() {
        assertTrue(listOf(null, null).isDeepNullOrEmpty())
    }

    @Test
    fun `collection with non-null values is not deep null or empty`() {
        assertFalse(listOf("a", "b").isDeepNullOrEmpty())
    }

    @Test
    fun `empty array is deep null or empty`() {
        assertTrue(emptyArray<String>().isDeepNullOrEmpty())
    }

    @Test
    fun `array with all nulls is deep null or empty`() {
        assertTrue(arrayOf<String?>(null, null).isDeepNullOrEmpty())
    }

    @Test
    fun `empty map is deep null or empty`() {
        assertTrue(emptyMap<String, String>().isDeepNullOrEmpty())
    }

    @Test
    fun `map with all null values is deep null or empty`() {
        assertTrue(mapOf("a" to null).isDeepNullOrEmpty())
    }

    @Test
    fun `map with non-null values is not deep null or empty`() {
        assertFalse(mapOf("a" to "b").isDeepNullOrEmpty())
    }

    @Test
    fun `enum is not deep null or empty`() {
        assertFalse(Thread.State.NEW.isDeepNullOrEmpty())
    }

    @Test
    fun `number is not deep null or empty`() {
        assertFalse(42.isDeepNullOrEmpty())
    }

    @Test
    fun `empty primitive arrays are deep null or empty`() {
        assertTrue(intArrayOf().isDeepNullOrEmpty())
        assertTrue(longArrayOf().isDeepNullOrEmpty())
        assertTrue(doubleArrayOf().isDeepNullOrEmpty())
        assertTrue(floatArrayOf().isDeepNullOrEmpty())
        assertTrue(booleanArrayOf().isDeepNullOrEmpty())
        assertTrue(charArrayOf().isDeepNullOrEmpty())
        assertTrue(byteArrayOf().isDeepNullOrEmpty())
        assertTrue(shortArrayOf().isDeepNullOrEmpty())
    }

    @Test
    fun `non-empty primitive arrays are not deep null or empty`() {
        assertFalse(intArrayOf(1).isDeepNullOrEmpty())
        assertFalse(booleanArrayOf(true).isDeepNullOrEmpty())
    }

    @Test
    fun `pair with both null is deep null or empty`() {
        assertTrue(Pair(null, null).isDeepNullOrEmpty())
    }

    @Test
    fun `pair with one non-null is not deep null or empty`() {
        assertFalse(Pair("a", null).isDeepNullOrEmpty())
    }

    @Test
    fun `triple with all null is deep null or empty`() {
        assertTrue(Triple(null, null, null).isDeepNullOrEmpty())
    }

    @Test
    fun `triple with one non-null is not deep null or empty`() {
        assertFalse(Triple(null, "b", null).isDeepNullOrEmpty())
    }

    @Test
    fun `nested collections with all blanks are deep null or empty`() {
        assertTrue(listOf("", "   ").isDeepNullOrEmpty())
    }
}
