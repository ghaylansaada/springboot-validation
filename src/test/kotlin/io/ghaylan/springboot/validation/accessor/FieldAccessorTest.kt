package io.ghaylan.springboot.validation.accessor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FieldAccessorTest {

    data class TestDto(val name: String = "test")

    private val accessor = AccessorFactory.build(TestDto::class.java, "name")

    @Test
    fun `getFromAny returns null for null instance`() {
        assertNull(accessor.getFromAny(null))
    }

    @Test
    fun `getFromAny returns value for correct type`() {
        assertEquals("test", accessor.getFromAny(TestDto()))
    }

    @Test
    fun `getFromAny returns null for wrong type non-strict`() {
        assertNull(accessor.getFromAny("wrong type", strict = false))
    }

    @Test
    fun `getFromAny throws for wrong type strict`() {
        assertThrows(IllegalStateException::class.java) {
            accessor.getFromAny("wrong type", strict = true)
        }
    }

    @Test
    fun `getFromAny works with subclass`() {
        open class Base(val field: String = "base")
        class Child : Base("child")

        val baseAccessor = AccessorFactory.build(Base::class.java, "field")
        assertEquals("child", baseAccessor.getFromAny(Child()))
    }
}
