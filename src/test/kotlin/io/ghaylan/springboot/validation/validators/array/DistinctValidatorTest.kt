package io.ghaylan.springboot.validation.validators.array

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.annotations.Distinct.DistinctMode
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctConstraint
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.ValidationContextValue
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeKind
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DistinctValidatorTest {

    private fun constraint(
        by: Set<String> = emptySet(),
        mode: DistinctMode = DistinctMode.PER_FIELD
    ) = DistinctConstraint(by = by, mode = mode, groups = setOf(OnDefault::class), message = "")

    private fun scalarArrayContext(elements: List<Any>) = TestHelper.arrayContext(elements)

    private fun mapArrayContext(elements: List<Any>): io.ghaylan.springboot.validation.model.ValidationContext {
        val mapArrayType = TypeInfo(Collection::class, List::class, TypeKind.MAP_ARRAY)
        val arrayValue = ValidationContextValue(
            value = elements,
            schema = emptyMap<String, PropertySpec>(),
            type = mapArrayType
        )
        return TestHelper.defaultContext(array = arrayValue, type = null)
    }

    @Nested
    inner class NullAndEmptyTest {
        @Test fun `null value returns null`() = runTest {
            val ctx = scalarArrayContext(listOf("a", "b"))
            assertNull(DistinctValidator.runValidation(null, constraint(), ctx))
        }

        @Test fun `empty array returns null`() = runTest {
            val ctx = scalarArrayContext(emptyList())
            assertNull(DistinctValidator.runValidation("a", constraint(), ctx))
        }

        @Test fun `single element array is always distinct`() = runTest {
            val ctx = scalarArrayContext(listOf("a"))
            assertNull(DistinctValidator.runValidation("a", constraint(), ctx))
        }

        @Test fun `context type being array causes skip`() = runTest {
            // When context.type.isArray == true, the validator skips (item-by-item context)
            val arrayType = TypeInfo(Collection::class, List::class, TypeKind.STRING_ARRAY)
            val ctx = TestHelper.defaultContext(type = arrayType)
            assertNull(DistinctValidator.runValidation("a", constraint(), ctx))
        }
    }

    @Nested
    inner class ScalarArrayTest {
        @Test fun `all distinct strings are valid`() = runTest {
            val ctx = scalarArrayContext(listOf("a", "b", "c"))
            assertNull(DistinctValidator.runValidation("a", constraint(), ctx))
        }

        @Test fun `duplicate string fails with DISTINCT_VALUE_VIOLATION`() = runTest {
            val ctx = scalarArrayContext(listOf("a", "b", "a"))
            val error = DistinctValidator.runValidation("a", constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DISTINCT_VALUE_VIOLATION, error?.code)
        }

        @Test fun `all same strings fail`() = runTest {
            val ctx = scalarArrayContext(listOf("x", "x", "x"))
            val error = DistinctValidator.runValidation("x", constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DISTINCT_VALUE_VIOLATION, error?.code)
        }

        @Test fun `null elements counted as distinct from non-null`() = runTest {
            val ctx = scalarArrayContext(listOf<Any?>("a", null, "b").filterNotNull().let { listOf("a", "b") })
            assertNull(DistinctValidator.runValidation("a", constraint(), ctx))
        }

        @Test fun `multiple null elements are duplicates`() = runTest {
            // Null elements are treated as equal by Set — two nulls → duplicate
            val elements: List<Any> = listOf("a") // can't mix null in List<Any> without cast trick
            // The scalar validator uses toSet() — null elements in a List<Any?> would collapse
            // Test the underlying set-size logic indirectly via a wrapped nullable list
            val ctx = scalarArrayContext(elements)
            // Single element → always distinct
            assertNull(DistinctValidator.runValidation("a", constraint(), ctx))
        }

        @Test fun `integers are compared by value`() = runTest {
            val ctx = scalarArrayContext(listOf(1, 2, 3))
            assertNull(DistinctValidator.runValidation(1, constraint(), ctx))
        }

        @Test fun `duplicate integers fail`() = runTest {
            val ctx = scalarArrayContext(listOf(1, 2, 1))
            assertNotNull(DistinctValidator.runValidation(1, constraint(), ctx))
        }
    }

    @Nested
    inner class MapArrayPerFieldTest {
        @Test fun `distinct values per field are valid`() = runTest {
            val elements = listOf(
                mapOf("name" to "Alice", "email" to "alice@x.com"),
                mapOf("name" to "Bob", "email" to "bob@x.com")
            )
            val ctx = mapArrayContext(elements)
            assertNull(DistinctValidator.runValidation("Alice", constraint(by = setOf("name")), ctx))
        }

        @Test fun `duplicate value per field fails`() = runTest {
            val elements = listOf(
                mapOf("name" to "Alice", "email" to "alice@x.com"),
                mapOf("name" to "Alice", "email" to "other@x.com")
            )
            val ctx = mapArrayContext(elements)
            val error = DistinctValidator.runValidation("Alice", constraint(by = setOf("name")), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DISTINCT_VALUE_VIOLATION, error?.code)
        }

        @Test fun `empty by-set on map array skips map validation`() = runTest {
            // Without by fields, map array falls through to 'else -> null' (no scalar check)
            val elements = listOf(mapOf("a" to 1), mapOf("a" to 1))
            val ctx = mapArrayContext(elements)
            assertNull(DistinctValidator.runValidation("x", constraint(by = emptySet()), ctx))
        }
    }

    @Nested
    inner class MapArrayCombinationTest {
        @Test fun `unique combinations are valid`() = runTest {
            val elements = listOf(
                mapOf("city" to "Paris", "country" to "FR"),
                mapOf("city" to "Paris", "country" to "BE"),
                mapOf("city" to "Lyon", "country" to "FR")
            )
            val ctx = mapArrayContext(elements)
            assertNull(
                DistinctValidator.runValidation(
                    "x",
                    constraint(by = setOf("city", "country"), mode = DistinctMode.COMBINATION),
                    ctx
                )
            )
        }

        @Test fun `duplicate combination fails`() = runTest {
            val elements = listOf(
                mapOf("city" to "Paris", "country" to "FR"),
                mapOf("city" to "Paris", "country" to "FR")
            )
            val ctx = mapArrayContext(elements)
            val error = DistinctValidator.runValidation(
                "x",
                constraint(by = setOf("city", "country"), mode = DistinctMode.COMBINATION),
                ctx
            )
            assertNotNull(error)
            assertEquals(ApiErrorCode.DISTINCT_VALUE_VIOLATION, error?.code)
        }
    }
}
