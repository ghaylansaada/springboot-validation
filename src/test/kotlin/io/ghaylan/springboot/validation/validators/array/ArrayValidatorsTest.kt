package io.ghaylan.springboot.validation.validators.array

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.array.size.ArraySizeConstraint
import io.ghaylan.springboot.validation.constraints.validators.array.size.ArraySizeValidator
import io.ghaylan.springboot.validation.constraints.validators.map.MapSizeConstraint
import io.ghaylan.springboot.validation.constraints.validators.map.MapSizeValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ArrayValidatorsTest {
	
	private val ctx = TestHelper.defaultContext()
	
	@Nested
	inner class ArraySizeValidatorTest {
		
		private fun constraint(
			min: Int = 0,
			max: Int = Int.MAX_VALUE
		) = ArraySizeConstraint(min = min, max = max, groups = setOf(OnDefault::class), message = "")
		
		@Test
		fun `null is valid`() = runTest {
			assertNull(ArraySizeValidator.runValidation(null, constraint(1, 5), ctx))
		}
		
		@Test
		fun `array within range is valid`() = runTest {
			assertNull(ArraySizeValidator.runValidation(arrayOf("a", "b", "c"), constraint(1, 5), ctx))
		}
		
		@Test
		fun `array at min boundary is valid`() = runTest {
			assertNull(ArraySizeValidator.runValidation(arrayOf("a"), constraint(1, 5), ctx))
		}
		
		@Test
		fun `array at max boundary is valid`() = runTest {
			assertNull(ArraySizeValidator.runValidation(arrayOf("a", "b", "c", "d", "e"), constraint(1, 5), ctx))
		}
		
		@Test
		fun `array too small fails`() = runTest {
			val error = ArraySizeValidator.runValidation(emptyArray<String>(), constraint(1, 5), ctx)
			assertNotNull(error)
			assertEquals(ApiErrorCode.ARRAY_SIZE_VIOLATION, error?.code)
		}
		
		@Test
		fun `array too large fails`() = runTest {
			val error = ArraySizeValidator.runValidation(arrayOf("a", "b", "c"), constraint(1, 2), ctx)
			assertNotNull(error)
			assertEquals(ApiErrorCode.ARRAY_SIZE_VIOLATION, error?.code)
		}
		
		@Test
		fun `collection within range is valid`() = runTest {
			assertNull(ArraySizeValidator.runValidation(listOf("a", "b"), constraint(1, 5), ctx))
		}
		
		@Test
		fun `collection too small fails`() = runTest {
			val error = ArraySizeValidator.runValidation(emptyList<String>(), constraint(1, 5), ctx)
			assertNotNull(error)
		}
		
		@Test
		fun `intArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(intArrayOf(1, 2, 3), constraint(1, 5), ctx))
		}
		
		@Test
		fun `longArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(longArrayOf(1L, 2L), constraint(1, 5), ctx))
		}
		
		@Test
		fun `booleanArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(booleanArrayOf(true, false), constraint(1, 5), ctx))
		}
		
		@Test
		fun `doubleArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(doubleArrayOf(1.0, 2.0), constraint(1, 5), ctx))
		}
		
		@Test
		fun `floatArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(floatArrayOf(1.0f), constraint(1, 5), ctx))
		}
		
		@Test
		fun `byteArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(byteArrayOf(1, 2), constraint(1, 5), ctx))
		}
		
		@Test
		fun `shortArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(shortArrayOf(1, 2), constraint(1, 5), ctx))
		}
		
		@Test
		fun `charArray works`() = runTest {
			assertNull(ArraySizeValidator.runValidation(charArrayOf('a', 'b'), constraint(1, 5), ctx))
		}
	}
	
	@Nested
	inner class MapSizeValidatorTest {
		
		private fun constraint(
			min: Int = 0,
			max: Int = Int.MAX_VALUE
		) = MapSizeConstraint(min = min, max = max, groups = setOf(OnDefault::class), message = "")
		
		@Test
		fun `null is valid`() = runTest {
			assertNull(MapSizeValidator.runValidation(null, constraint(1, 5), ctx))
		}
		
		@Test
		fun `map within range is valid`() = runTest {
			assertNull(MapSizeValidator.runValidation(mapOf("a" to 1, "b" to 2), constraint(1, 5), ctx))
		}
		
		@Test
		fun `map at min boundary is valid`() = runTest {
			assertNull(MapSizeValidator.runValidation(mapOf("a" to 1), constraint(1, 5), ctx))
		}
		
		@Test
		fun `map too small fails`() = runTest {
			val error = MapSizeValidator.runValidation(emptyMap<String, Any>(), constraint(1, 5), ctx)
			assertNotNull(error)
			assertEquals(ApiErrorCode.OBJECT_SIZE_VIOLATION, error?.code)
		}
		
		@Test
		fun `map too large fails`() = runTest {
			val error = MapSizeValidator.runValidation(mapOf("a" to 1, "b" to 2, "c" to 3), constraint(1, 2), ctx)
			assertNotNull(error)
		}
	}
}
