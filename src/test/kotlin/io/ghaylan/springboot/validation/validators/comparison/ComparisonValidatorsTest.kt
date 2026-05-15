package io.ghaylan.springboot.validation.validators.comparison

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.comparison.equal.EqualToConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.equal.EqualToValidator
import io.ghaylan.springboot.validation.constraints.validators.comparison.greaterthan.GreaterThanConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.greaterthan.GreaterThanValidator
import io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan.LessThanConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan.LessThanValidator
import io.ghaylan.springboot.validation.constraints.validators.comparison.notequal.NotEqualToConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.notequal.NotEqualToValidator
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuein.ValueInConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuein.ValueInValidator
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuenotin.ValueNotInConstraint
import io.ghaylan.springboot.validation.constraints.validators.comparison.valuenotin.ValueNotInValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ComparisonValidatorsTest {

    data class ComparisonDto(
        val fieldA: Int = 10,
        val fieldB: Int = 20,
        val fieldC: String = "hello",
        val fieldD: Double = 3.14
    )

    @Nested
    inner class EqualToValidatorTest {
        private fun constraint(property: String) = EqualToConstraint(
            property = property,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            val ctx = TestHelper.contextWithSiblingProperty(ComparisonDto(), ComparisonDto::class.java)
            assertNull(EqualToValidator.runValidation(null, constraint("fieldB"), ctx))
        }

        @Test fun `equal values are valid`() = runTest {
            val dto = ComparisonDto(fieldA = 10, fieldB = 10)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            assertNull(EqualToValidator.runValidation(10, constraint("fieldB"), ctx))
        }

        @Test fun `unequal values fail with EQUALITY_VIOLATION`() = runTest {
            val dto = ComparisonDto(fieldA = 10, fieldB = 20)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = EqualToValidator.runValidation(10, constraint("fieldB"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.EQUALITY_VIOLATION, error?.code)
        }

        @Test fun `different types fail with DEPENDENCY_TYPE_VIOLATION`() = runTest {
            val dto = ComparisonDto(fieldA = 10, fieldC = "hello")
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = EqualToValidator.runValidation(10, constraint("fieldC"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DEPENDENCY_TYPE_VIOLATION, error?.code)
        }

        @Test fun `equal negative integers are valid`() = runTest {
            data class Dto(val a: Int = -5, val b: Int = -5)
            val dto = Dto()
            val ctx = TestHelper.contextWithSiblingProperty(dto, Dto::class.java)
            assertNull(EqualToValidator.runValidation(-5, EqualToConstraint("b", setOf(OnDefault::class), ""), ctx))
        }

        @Test fun `sibling property null returns error`() = runTest {
            // When sibling is null, EqualToValidator returns EQUALITY_VIOLATION (non-null value can't equal null)
            data class NullableDto(val a: Int = 5, val b: Int? = null)
            val dto = NullableDto()
            val ctx = TestHelper.contextWithSiblingProperty(dto, NullableDto::class.java)
            val error = EqualToValidator.runValidation(5, EqualToConstraint("b", setOf(OnDefault::class), ""), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.EQUALITY_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class GreaterThanValidatorTest {
        private fun constraint(property: String, inclusive: Boolean = false) = GreaterThanConstraint(
            property = property, inclusive = inclusive,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            val ctx = TestHelper.contextWithSiblingProperty(ComparisonDto(), ComparisonDto::class.java)
            assertNull(GreaterThanValidator.runValidation(null, constraint("fieldA"), ctx))
        }

        @Test fun `greater value is valid`() = runTest {
            val dto = ComparisonDto(fieldA = 10)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            assertNull(GreaterThanValidator.runValidation(20, constraint("fieldA"), ctx))
        }

        @Test fun `equal value with inclusive is valid`() = runTest {
            val dto = ComparisonDto(fieldA = 10)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            assertNull(GreaterThanValidator.runValidation(10, constraint("fieldA", inclusive = true), ctx))
        }

        @Test fun `equal value without inclusive fails`() = runTest {
            val dto = ComparisonDto(fieldA = 10)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = GreaterThanValidator.runValidation(10, constraint("fieldA", inclusive = false), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.GREATER_THAN_VIOLATION, error?.code)
        }

        @Test fun `lesser value fails`() = runTest {
            val dto = ComparisonDto(fieldA = 10)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = GreaterThanValidator.runValidation(5, constraint("fieldA"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.GREATER_THAN_VIOLATION, error?.code)
        }

        @Test fun `different types fail with DEPENDENCY_TYPE_VIOLATION`() = runTest {
            val dto = ComparisonDto(fieldA = 10, fieldC = "hello")
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = GreaterThanValidator.runValidation(10, constraint("fieldC"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DEPENDENCY_TYPE_VIOLATION, error?.code)
        }

        @Test fun `sibling is null returns null (skipped)`() = runTest {
            data class NullableDto(val a: Int = 5, val b: Int? = null)
            val ctx = TestHelper.contextWithSiblingProperty(NullableDto(), NullableDto::class.java)
            assertNull(GreaterThanValidator.runValidation(5, GreaterThanConstraint("b", false, setOf(OnDefault::class), ""), ctx))
        }
    }

    @Nested
    inner class LessThanValidatorTest {
        private fun constraint(property: String, inclusive: Boolean = false) = LessThanConstraint(
            property = property, inclusive = inclusive,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            val ctx = TestHelper.contextWithSiblingProperty(ComparisonDto(), ComparisonDto::class.java)
            assertNull(LessThanValidator.runValidation(null, constraint("fieldB"), ctx))
        }

        @Test fun `lesser value is valid`() = runTest {
            val dto = ComparisonDto(fieldB = 20)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            assertNull(LessThanValidator.runValidation(10, constraint("fieldB"), ctx))
        }

        @Test fun `equal value with inclusive is valid`() = runTest {
            val dto = ComparisonDto(fieldB = 20)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            assertNull(LessThanValidator.runValidation(20, constraint("fieldB", inclusive = true), ctx))
        }

        @Test fun `equal value without inclusive fails`() = runTest {
            val dto = ComparisonDto(fieldB = 20)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = LessThanValidator.runValidation(20, constraint("fieldB", inclusive = false), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.LESS_THAN_VIOLATION, error?.code)
        }

        @Test fun `greater value fails`() = runTest {
            val dto = ComparisonDto(fieldB = 20)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = LessThanValidator.runValidation(30, constraint("fieldB"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.LESS_THAN_VIOLATION, error?.code)
        }

        @Test fun `different types fail with DEPENDENCY_TYPE_VIOLATION`() = runTest {
            val dto = ComparisonDto(fieldB = 20, fieldC = "hello")
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = LessThanValidator.runValidation(10, constraint("fieldC"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DEPENDENCY_TYPE_VIOLATION, error?.code)
        }

        @Test fun `sibling is null returns null (skipped)`() = runTest {
            data class NullableDto(val a: Int = 5, val b: Int? = null)
            val ctx = TestHelper.contextWithSiblingProperty(NullableDto(), NullableDto::class.java)
            assertNull(LessThanValidator.runValidation(5, LessThanConstraint("b", false, setOf(OnDefault::class), ""), ctx))
        }
    }

    @Nested
    inner class NotEqualToValidatorTest {
        private fun constraint(property: String) = NotEqualToConstraint(
            property = property,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            val ctx = TestHelper.contextWithSiblingProperty(ComparisonDto(), ComparisonDto::class.java)
            assertNull(NotEqualToValidator.runValidation(null, constraint("fieldA"), ctx))
        }

        @Test fun `different values are valid`() = runTest {
            val dto = ComparisonDto(fieldA = 10)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            assertNull(NotEqualToValidator.runValidation(20, constraint("fieldA"), ctx))
        }

        @Test fun `equal values fail with INEQUALITY_VIOLATION`() = runTest {
            val dto = ComparisonDto(fieldA = 10)
            val ctx = TestHelper.contextWithSiblingProperty(dto, ComparisonDto::class.java)
            val error = NotEqualToValidator.runValidation(10, constraint("fieldA"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.INEQUALITY_VIOLATION, error?.code)
        }

        @Test fun `negative values that differ are valid`() = runTest {
            data class Dto(val a: Int = -5, val b: Int = -3)
            val ctx = TestHelper.contextWithSiblingProperty(Dto(), Dto::class.java)
            assertNull(NotEqualToValidator.runValidation(-3, NotEqualToConstraint("a", setOf(OnDefault::class), ""), ctx))
        }
    }

    @Nested
    inner class ValueInValidatorTest {
        private val ctx = TestHelper.defaultContext()

        private fun constraint(vararg values: String) = ValueInConstraint(
            values = values.toSet(),
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(ValueInValidator.runValidation(null, constraint("a", "b"), ctx))
        }

        @Test fun `non-scalar collection is skipped`() = runTest {
            assertNull(ValueInValidator.runValidation(listOf("a"), constraint("a"), ctx))
        }

        @Test fun `value in allowed set is valid`() = runTest {
            assertNull(ValueInValidator.runValidation("a", constraint("a", "b", "c"), ctx))
        }

        @Test fun `value not in allowed set fails with ALLOWED_VALUE_VIOLATION`() = runTest {
            val error = ValueInValidator.runValidation("d", constraint("a", "b", "c"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.ALLOWED_VALUE_VIOLATION, error?.code)
        }

        @Test fun `integer value converted to string and matched`() = runTest {
            assertNull(ValueInValidator.runValidation(1, constraint("1", "2", "3"), ctx))
        }

        @Test fun `integer value not in set fails`() = runTest {
            val error = ValueInValidator.runValidation(99, constraint("1", "2", "3"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.ALLOWED_VALUE_VIOLATION, error?.code)
        }

        @Test fun `empty allowed set always fails for non-null scalar`() = runTest {
            val error = ValueInValidator.runValidation("anything", constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.ALLOWED_VALUE_VIOLATION, error?.code)
        }

        @Test fun `case sensitive match - wrong case fails`() = runTest {
            val error = ValueInValidator.runValidation("ADMIN", constraint("admin", "user"), ctx)
            assertNotNull(error)
        }
    }

    @Nested
    inner class ValueNotInValidatorTest {
        private val ctx = TestHelper.defaultContext()

        private fun constraint(vararg values: String) = ValueNotInConstraint(
            values = values.toSet(),
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(ValueNotInValidator.runValidation(null, constraint("a", "b"), ctx))
        }

        @Test fun `non-scalar collection is skipped`() = runTest {
            assertNull(ValueNotInValidator.runValidation(listOf("a"), constraint("a"), ctx))
        }

        @Test fun `value not in forbidden set is valid`() = runTest {
            assertNull(ValueNotInValidator.runValidation("d", constraint("a", "b", "c"), ctx))
        }

        @Test fun `value in forbidden set fails with DISALLOWED_VALUE_VIOLATION`() = runTest {
            val error = ValueNotInValidator.runValidation("a", constraint("a", "b", "c"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DISALLOWED_VALUE_VIOLATION, error?.code)
        }

        @Test fun `integer value in forbidden set fails`() = runTest {
            val error = ValueNotInValidator.runValidation(1, constraint("1", "2", "3"), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DISALLOWED_VALUE_VIOLATION, error?.code)
        }

        @Test fun `empty forbidden set always valid`() = runTest {
            assertNull(ValueNotInValidator.runValidation("anything", constraint(), ctx))
        }

        @Test fun `case sensitive - wrong case is valid (not in set)`() = runTest {
            assertNull(ValueNotInValidator.runValidation("BANNED", constraint("banned", "locked"), ctx))
        }
    }
}
