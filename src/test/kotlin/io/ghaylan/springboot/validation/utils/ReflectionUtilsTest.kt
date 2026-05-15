package io.ghaylan.springboot.validation.utils

import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ReflectionUtilsTest {

    data class SimpleDto(val name: String, val age: Int)
    data class NestedDto(val inner: SimpleDto)
    data class ListDto(val items: List<String>)
    data class MapDto(val data: Map<String, Any>)

    @Nested
    inner class IsObjectLikeTest {
        @Test fun `data class is object-like`() {
            assertTrue(ReflectionUtils.isObjectLike(SimpleDto::class.java))
        }

        @Test fun `String is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(String::class.java))
        }

        @Test fun `Int is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(Int::class.java))
        }

        @Test fun `List is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(List::class.java))
        }

        @Test fun `Map is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(Map::class.java))
        }

        @Test fun `Array is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(Array<String>::class.java))
        }

        @Test fun `enum is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(TypeKind::class.java))
        }

        @Test fun `interface is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(Comparable::class.java))
        }

        @Test fun `LocalDate is not object-like`() {
            assertFalse(ReflectionUtils.isObjectLike(LocalDate::class.java))
        }
    }

    @Nested
    inner class IsCollectionLikeTest {
        @Test fun `List is collection-like`() {
            assertTrue(ReflectionUtils.isCollectionLike(List::class.java))
        }

        @Test fun `Set is collection-like`() {
            assertTrue(ReflectionUtils.isCollectionLike(Set::class.java))
        }

        @Test fun `array is collection-like`() {
            assertTrue(ReflectionUtils.isCollectionLike(Array<String>::class.java))
        }

        @Test fun `String is not collection-like`() {
            assertFalse(ReflectionUtils.isCollectionLike(String::class.java))
        }

        @Test fun `Map is not collection-like`() {
            assertFalse(ReflectionUtils.isCollectionLike(Map::class.java))
        }
    }

    @Nested
    inner class IsMapLikeTest {
        @Test fun `Map is map-like`() {
            assertTrue(ReflectionUtils.isMapLike(Map::class.java))
        }

        @Test fun `HashMap is map-like`() {
            assertTrue(ReflectionUtils.isMapLike(HashMap::class.java))
        }

        @Test fun `List is not map-like`() {
            assertFalse(ReflectionUtils.isMapLike(List::class.java))
        }
    }

    @Nested
    inner class IsCollectionTest {
        @Test fun `array is collection`() {
            assertTrue(ReflectionUtils.isCollection(arrayOf("a")))
        }

        @Test fun `list is collection`() {
            assertTrue(ReflectionUtils.isCollection(listOf("a")))
        }

        @Test fun `intArray is collection`() {
            assertTrue(ReflectionUtils.isCollection(intArrayOf(1)))
        }

        @Test fun `string is not collection`() {
            assertFalse(ReflectionUtils.isCollection("hello"))
        }
    }

    @Nested
    inner class IsScalarTest {
        @Test fun `string is scalar`() {
            assertTrue(ReflectionUtils.isScalar("hello"))
        }

        @Test fun `int is scalar`() {
            assertTrue(ReflectionUtils.isScalar(42))
        }

        @Test fun `boolean is scalar`() {
            assertTrue(ReflectionUtils.isScalar(true))
        }

        @Test fun `LocalDate is scalar`() {
            assertTrue(ReflectionUtils.isScalar(LocalDate.now()))
        }

        @Test fun `enum is scalar`() {
            assertTrue(ReflectionUtils.isScalar(TypeKind.STRING))
        }

        @Test fun `list is not scalar`() {
            assertFalse(ReflectionUtils.isScalar(listOf("a")))
        }
    }

    @Nested
    inner class InfoFromFieldTest {
        @Test fun `resolves String field`() {
            val field = SimpleDto::class.java.getDeclaredField("name")
            val info = ReflectionUtils.infoFromField(field)
            assertEquals(String::class, info.concreteType)
            assertEquals(TypeKind.STRING, info.kind)
        }

        @Test fun `resolves Int field`() {
            val field = SimpleDto::class.java.getDeclaredField("age")
            val info = ReflectionUtils.infoFromField(field)
            assertEquals(Int::class, info.concreteType)
            assertEquals(TypeKind.NUMERIC, info.kind)
        }

        @Test fun `resolves List field`() {
            val field = ListDto::class.java.getDeclaredField("items")
            val info = ReflectionUtils.infoFromField(field)
            assertTrue(info.isArray)
            assertEquals(TypeKind.STRING_ARRAY, info.kind)
            assertEquals(1, info.typeArguments.size)
            assertEquals(String::class, info.typeArguments[0].concreteType)
        }

        @Test fun `resolves nested object field`() {
            val field = NestedDto::class.java.getDeclaredField("inner")
            val info = ReflectionUtils.infoFromField(field)
            assertTrue(info.isObject)
            assertEquals(SimpleDto::class, info.concreteType)
        }

        @Test fun `resolves Map field`() {
            val field = MapDto::class.java.getDeclaredField("data")
            val info = ReflectionUtils.infoFromField(field)
            assertTrue(info.isMap)
            assertEquals(TypeKind.MAP, info.kind)
        }
    }

    @Nested
    inner class TypeInfoPropertiesTest {
        @Test fun `isArray for string array type`() {
            val field = ListDto::class.java.getDeclaredField("items")
            val info = ReflectionUtils.infoFromField(field)
            assertTrue(info.isArray)
            assertFalse(info.isObject)
            assertFalse(info.isMap)
        }

        @Test fun `isObject for data class`() {
            val field = NestedDto::class.java.getDeclaredField("inner")
            val info = ReflectionUtils.infoFromField(field)
            assertTrue(info.isObject)
            assertFalse(info.isArray)
        }

        @Test fun `isScalar for String`() {
            val field = SimpleDto::class.java.getDeclaredField("name")
            val info = ReflectionUtils.infoFromField(field)
            assertTrue(info.isScalar)
            assertFalse(info.isArray)
            assertFalse(info.isObject)
        }

        @Test fun `resolveType returns element type for array`() {
            val field = ListDto::class.java.getDeclaredField("items")
            val info = ReflectionUtils.infoFromField(field)
            assertEquals(String::class, info.resolveType)
        }

        @Test fun `resolveType returns concrete type for non-array`() {
            val field = SimpleDto::class.java.getDeclaredField("name")
            val info = ReflectionUtils.infoFromField(field)
            assertEquals(String::class, info.resolveType)
        }
    }

    open class InheritanceParent(val parentField: String = "p")
    class InheritanceChild(val childField: String = "c") : InheritanceParent()

    @Nested
    inner class GetFieldsTest {
        @Test fun `getFields includes parent fields`() {
            val fields = ReflectionUtils.getFields(InheritanceChild::class.java)
            val names = fields.map { it.name }
            assertTrue("childField" in names)
            assertTrue("parentField" in names)
        }

        @Test fun `getFields for simple class`() {
            val fields = ReflectionUtils.getFields(SimpleDto::class.java)
            assertEquals(2, fields.size)
        }
    }

    @Nested
    inner class InfoFromClassTest {
        @Test fun `infoFromType resolves data class`() {
            val info = ReflectionUtils.infoFromType(SimpleDto::class.java)
            assertTrue(info.isObject)
            assertEquals(SimpleDto::class, info.concreteType)
        }

        @Test fun `infoFromClass resolves String type`() {
            val info = ReflectionUtils.infoFromType(String::class.java)
            assertEquals(TypeKind.STRING, info.kind)
        }
    }

    @Nested
    inner class TypeKindTest {
        @Test fun `scalar kinds have isScalar true`() {
            assertTrue(TypeKind.STRING.isScalar)
            assertTrue(TypeKind.NUMERIC.isScalar)
            assertTrue(TypeKind.BOOLEAN.isScalar)
            assertTrue(TypeKind.DATE.isScalar)
            assertTrue(TypeKind.ENUM.isScalar)
        }

        @Test fun `non-scalar kinds have isScalar false`() {
            assertFalse(TypeKind.OBJECT.isScalar)
            assertFalse(TypeKind.MAP.isScalar)
            assertFalse(TypeKind.ANY.isScalar)
        }

        @Test fun `array kinds have isArray true`() {
            assertTrue(TypeKind.STRING_ARRAY.isArray)
            assertTrue(TypeKind.OBJECT_ARRAY.isArray)
            assertTrue(TypeKind.NUMERIC_ARRAY.isArray)
            assertTrue(TypeKind.ARRAY_ARRAY.isArray)
        }

        @Test fun `non-array kinds have isArray false`() {
            assertFalse(TypeKind.STRING.isArray)
            assertFalse(TypeKind.OBJECT.isArray)
            assertFalse(TypeKind.MAP.isArray)
        }
    }
}
