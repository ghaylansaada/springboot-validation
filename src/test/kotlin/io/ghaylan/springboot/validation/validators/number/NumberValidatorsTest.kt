package io.ghaylan.springboot.validation.validators.number

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.number.divisible.DivisibleByConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.divisible.DivisibleByValidator
import io.ghaylan.springboot.validation.constraints.validators.number.latitude.LatitudeConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.latitude.LatitudeValidator
import io.ghaylan.springboot.validation.constraints.validators.number.longitude.LongitudeConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.longitude.LongitudeValidator
import io.ghaylan.springboot.validation.constraints.validators.number.max.NumberMaxConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.max.NumberMaxValidator
import io.ghaylan.springboot.validation.constraints.validators.number.min.NumberMinConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.min.NumberMinValidator
import io.ghaylan.springboot.validation.constraints.validators.number.multiple.MultipleOfConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.multiple.MultipleOfValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NumberValidatorsTest {

    private val ctx = TestHelper.defaultContext()

    @Nested
    inner class NumberMinValidatorTest {
        private fun constraint(value: Double, inclusive: Boolean = true) =
            NumberMinConstraint(value = value, inclusive = inclusive, groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(NumberMinValidator.runValidation(null, constraint(0.0), ctx))
        }

        @Test fun `value above min is valid`() = runTest {
            assertNull(NumberMinValidator.runValidation(10, constraint(5.0), ctx))
        }

        @Test fun `value at min inclusive is valid`() = runTest {
            assertNull(NumberMinValidator.runValidation(5, constraint(5.0, inclusive = true), ctx))
        }

        @Test fun `value at min exclusive fails`() = runTest {
            val error = NumberMinValidator.runValidation(5, constraint(5.0, inclusive = false), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.MIN_VALUE_VIOLATION, error?.code)
        }

        @Test fun `value below min fails`() = runTest {
            val error = NumberMinValidator.runValidation(3, constraint(5.0), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.MIN_VALUE_VIOLATION, error?.code)
        }

        @Test fun `works with Double`() = runTest {
            assertNull(NumberMinValidator.runValidation(5.5, constraint(5.0), ctx))
        }

        @Test fun `works with Long`() = runTest {
            assertNull(NumberMinValidator.runValidation(100L, constraint(50.0), ctx))
        }
    }

    @Nested
    inner class NumberMaxValidatorTest {
        private fun constraint(value: Double, inclusive: Boolean = true) =
            NumberMaxConstraint(value = value, inclusive = inclusive, groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(NumberMaxValidator.runValidation(null, constraint(100.0), ctx))
        }

        @Test fun `value below max is valid`() = runTest {
            assertNull(NumberMaxValidator.runValidation(50, constraint(100.0), ctx))
        }

        @Test fun `value at max inclusive is valid`() = runTest {
            assertNull(NumberMaxValidator.runValidation(100, constraint(100.0, inclusive = true), ctx))
        }

        @Test fun `value at max exclusive fails`() = runTest {
            val error = NumberMaxValidator.runValidation(100, constraint(100.0, inclusive = false), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.MAX_VALUE_VIOLATION, error?.code)
        }

        @Test fun `value above max fails`() = runTest {
            val error = NumberMaxValidator.runValidation(150, constraint(100.0), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.MAX_VALUE_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class DivisibleByValidatorTest {
        private fun constraint(divisor: Double) =
            DivisibleByConstraint(divisor = divisor, groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(DivisibleByValidator.runValidation(null, constraint(2.0), ctx))
        }

        @Test fun `divisible value is valid`() = runTest {
            assertNull(DivisibleByValidator.runValidation(10, constraint(5.0), ctx))
        }

        @Test fun `non-divisible value fails`() = runTest {
            val error = DivisibleByValidator.runValidation(7, constraint(3.0), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DIVISIBILITY_VIOLATION, error?.code)
        }

        @Test fun `zero is divisible by any number`() = runTest {
            assertNull(DivisibleByValidator.runValidation(0, constraint(5.0), ctx))
        }

        @Test fun `floating point divisibility`() = runTest {
            assertNull(DivisibleByValidator.runValidation(1.5, constraint(0.5), ctx))
        }
    }

    @Nested
    inner class MultipleOfValidatorTest {
        private fun constraint(factor: Double) =
            MultipleOfConstraint(factor = factor, groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(MultipleOfValidator.runValidation(null, constraint(3.0), ctx))
        }

        @Test fun `multiple value is valid`() = runTest {
            assertNull(MultipleOfValidator.runValidation(15, constraint(5.0), ctx))
        }

        @Test fun `non-multiple fails`() = runTest {
            val error = MultipleOfValidator.runValidation(7, constraint(3.0), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.MULTIPLICITY_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class LatitudeValidatorTest {
        private val constraint = LatitudeConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(LatitudeValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid latitude 0`() = runTest {
            assertNull(LatitudeValidator.runValidation(0.0, constraint, ctx))
        }

        @Test fun `valid latitude at boundary -90`() = runTest {
            assertNull(LatitudeValidator.runValidation(-90.0, constraint, ctx))
        }

        @Test fun `valid latitude at boundary 90`() = runTest {
            assertNull(LatitudeValidator.runValidation(90.0, constraint, ctx))
        }

        @Test fun `invalid latitude above 90`() = runTest {
            val error = LatitudeValidator.runValidation(91.0, constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.LATITUDE_VIOLATION, error?.code)
        }

        @Test fun `invalid latitude below -90`() = runTest {
            assertNotNull(LatitudeValidator.runValidation(-91.0, constraint, ctx))
        }
    }

    @Nested
    inner class LongitudeValidatorTest {
        private val constraint = LongitudeConstraint(groups = setOf(OnDefault::class), message = "")

        @Test fun `null is valid`() = runTest {
            assertNull(LongitudeValidator.runValidation(null, constraint, ctx))
        }

        @Test fun `valid longitude 0`() = runTest {
            assertNull(LongitudeValidator.runValidation(0.0, constraint, ctx))
        }

        @Test fun `valid longitude at boundary -180`() = runTest {
            assertNull(LongitudeValidator.runValidation(-180.0, constraint, ctx))
        }

        @Test fun `valid longitude at boundary 180`() = runTest {
            assertNull(LongitudeValidator.runValidation(180.0, constraint, ctx))
        }

        @Test fun `invalid longitude above 180`() = runTest {
            val error = LongitudeValidator.runValidation(181.0, constraint, ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.LONGITUDE_VIOLATION, error?.code)
        }

        @Test fun `invalid longitude below -180`() = runTest {
            assertNotNull(LongitudeValidator.runValidation(-181.0, constraint, ctx))
        }
    }
}
