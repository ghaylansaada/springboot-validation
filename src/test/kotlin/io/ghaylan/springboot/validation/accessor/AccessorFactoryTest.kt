package io.ghaylan.springboot.validation.accessor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AccessorFactoryTest {
	
	data class SimpleDto(
		val name: String = "test",
		val age: Int = 25
	)
	
	open class ParentDto(val parentField: String = "parent")
	class ChildDto(val childField: String = "child"): ParentDto("inherited")
	
	@Test
	fun `builds accessor for data class property`() {
		val accessor = AccessorFactory.build(SimpleDto::class.java, "name")
		val dto = SimpleDto(name = "hello")
		assertEquals("hello", accessor.get(dto))
	}
	
	@Test
	fun `builds accessor for int property`() {
		val accessor = AccessorFactory.build(SimpleDto::class.java, "age")
		val dto = SimpleDto(age = 30)
		assertEquals(30, accessor.get(dto))
	}
	
	@Test
	fun `builds accessor for inherited field`() {
		val accessor = AccessorFactory.build(ChildDto::class.java, "parentField")
		val dto = ChildDto()
		assertEquals("inherited", accessor.get(dto))
	}
	
	@Test
	fun `builds accessor for child field`() {
		val accessor = AccessorFactory.build(ChildDto::class.java, "childField")
		val dto = ChildDto(childField = "myChild")
		assertEquals("myChild", accessor.get(dto))
	}
	
	@Test
	fun `builds accessor for map`() {
		@Suppress("UNCHECKED_CAST")
		val accessor = AccessorFactory.build(Map::class.java as Class<Map<String, Any>>, "key")
		val map = mapOf("key" to "value")
		assertEquals("value", accessor.get(map))
	}
	
	@Test
	fun `map accessor returns null for missing key`() {
		@Suppress("UNCHECKED_CAST")
		val accessor = AccessorFactory.build(Map::class.java as Class<Map<String, Any>>, "missing")
		val map = mapOf("key" to "value")
		assertNull(accessor.get(map))
	}
	
	@Test
	fun `throws for non-existent field on non-map`() {
		assertThrows(IllegalStateException::class.java) {
			AccessorFactory.build(SimpleDto::class.java, "nonExistent")
		}
	}
	
	@Test
	fun `containerClass matches requested class`() {
		val accessor = AccessorFactory.build(SimpleDto::class.java, "name")
		assertEquals(SimpleDto::class.java, accessor.containerClass)
	}
}
