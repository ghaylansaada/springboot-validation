package io.ghaylan.springboot.validation.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CollectionUtilsTest {

    @Test
    fun `normalizeList returns empty for null`() {
        assertEquals(emptyList<Any>(), CollectionUtils.normalizeList(null))
    }

    @Test
    fun `normalizeList returns empty for non-collection type`() {
        assertEquals(emptyList<Any>(), CollectionUtils.normalizeList("hello"))
        assertEquals(emptyList<Any>(), CollectionUtils.normalizeList(42))
    }

    @Test
    fun `normalizeList handles reference array without nulls`() {
        val arr = arrayOf("a", "b", "c")
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(3, result.size)
        assertEquals("a", result[0])
        assertEquals("b", result[1])
        assertEquals("c", result[2])
    }

    @Test
    fun `normalizeList filters nulls from reference array`() {
        val arr = arrayOf("a", null, "c")
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
        assertEquals("a", result[0])
        assertEquals("c", result[1])
    }

    @Test
    fun `normalizeList handles empty array`() {
        val arr = emptyArray<String>()
        val result = CollectionUtils.normalizeList(arr)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `normalizeList handles list without nulls`() {
        val list = listOf("x", "y", "z")
        val result = CollectionUtils.normalizeList(list)
        assertEquals(3, result.size)
        assertEquals("x", result[0])
    }

    @Test
    fun `normalizeList filters nulls from list`() {
        val list = listOf("x", null, "z")
        val result = CollectionUtils.normalizeList(list)
        assertEquals(2, result.size)
        assertEquals("x", result[0])
        assertEquals("z", result[1])
    }

    @Test
    fun `normalizeList handles empty list`() {
        val result = CollectionUtils.normalizeList(emptyList<String>())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `normalizeList handles set (non-list collection)`() {
        val set = setOf("a", "b")
        val result = CollectionUtils.normalizeList(set)
        assertEquals(2, result.size)
    }

    @Test
    fun `normalizeList handles empty collection`() {
        val result = CollectionUtils.normalizeList(emptySet<String>())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `normalizeList handles IntArray`() {
        val arr = intArrayOf(1, 2, 3)
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(3, result.size)
        assertEquals(1, result[0])
    }

    @Test
    fun `normalizeList handles BooleanArray`() {
        val arr = booleanArrayOf(true, false)
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
        assertEquals(true, result[0])
    }

    @Test
    fun `normalizeList handles LongArray`() {
        val arr = longArrayOf(100L, 200L)
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
        assertEquals(100L, result[0])
    }

    @Test
    fun `normalizeList handles DoubleArray`() {
        val arr = doubleArrayOf(1.1, 2.2)
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
        assertEquals(1.1, result[0])
    }

    @Test
    fun `normalizeList handles FloatArray`() {
        val arr = floatArrayOf(1.1f, 2.2f)
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
        assertEquals(1.1f, result[0])
    }

    @Test
    fun `normalizeList handles ShortArray`() {
        val arr = shortArrayOf(1, 2)
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
    }

    @Test
    fun `normalizeList handles ByteArray`() {
        val arr = byteArrayOf(1, 2)
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
    }

    @Test
    fun `normalizeList returns view backed by array`() {
        val arr = arrayOf("a", "b")
        val result = CollectionUtils.normalizeList(arr)
        assertEquals(2, result.size)
        assertEquals("a", result[0])
        assertEquals("b", result[1])
    }
}
