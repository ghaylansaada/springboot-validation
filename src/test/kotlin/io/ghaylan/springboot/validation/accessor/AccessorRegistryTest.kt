package io.ghaylan.springboot.validation.accessor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccessorRegistryTest {
	
	data class TestDto(
		val name: String = "test",
		val value: Int = 42
	)
	
	@BeforeEach
	fun setup() {
		AccessorRegistry.clear()
	}
	
	@Test
	fun `getOrCreate returns accessor`() {
		val accessor = AccessorRegistry.getOrCreate(TestDto::class.java, "name", "name")
		assertNotNull(accessor)
		assertEquals("test", accessor.get(TestDto()))
	}
	
	@Test
	fun `getOrCreate caches accessor`() {
		val a1 = AccessorRegistry.getOrCreate(TestDto::class.java, "name", "name")
		val a2 = AccessorRegistry.getOrCreate(TestDto::class.java, "name", "name")
		assertSame(a1, a2)
	}
	
	@Test
	fun `different fields return different accessors`() {
		val a1 = AccessorRegistry.getOrCreate(TestDto::class.java, "name", "name")
		val a2 = AccessorRegistry.getOrCreate(TestDto::class.java, "value", "value")
		assertNotSame(a1, a2)
	}
	
	@Test
	fun `clear removes all cached accessors`() {
		val a1 = AccessorRegistry.getOrCreate(TestDto::class.java, "name", "name")
		AccessorRegistry.clear()
		val a2 = AccessorRegistry.getOrCreate(TestDto::class.java, "name", "name")
		assertNotSame(a1, a2)
	}
}
