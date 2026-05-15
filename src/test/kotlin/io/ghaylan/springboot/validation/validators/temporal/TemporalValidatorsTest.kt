package io.ghaylan.springboot.validation.validators.temporal

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays.AllowedDaysConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays.AllowedDaysValidator
import io.ghaylan.springboot.validation.constraints.validators.temporal.future.FutureConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.future.FutureValidator
import io.ghaylan.springboot.validation.constraints.validators.temporal.max.TemporalMaxConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.max.TemporalMaxValidator
import io.ghaylan.springboot.validation.constraints.validators.temporal.min.TemporalMinConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.min.TemporalMinValidator
import io.ghaylan.springboot.validation.constraints.validators.temporal.past.PastConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.past.PastValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TemporalValidatorsTest {

    private val ctx = TestHelper.defaultContext()

    @Nested
    inner class FutureValidatorTest {
        private fun constraint(
            withinDays: Long = 0, withinHours: Long = 0, withinMinutes: Long = 0,
            withinYears: Long = 0, withinMonths: Long = 0, withinWeeks: Long = 0, withinSeconds: Long = 0
        ) = FutureConstraint(
            withinSeconds = withinSeconds, withinMinutes = withinMinutes, withinHours = withinHours,
            withinDays = withinDays, withinWeeks = withinWeeks, withinMonths = withinMonths, withinYears = withinYears,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(FutureValidator.runValidation(null, constraint(), ctx))
        }

        @Test fun `future date is valid`() = runTest {
            val futureDate = LocalDate.now().plusDays(10)
            assertNull(FutureValidator.runValidation(futureDate, constraint(), ctx))
        }

        @Test fun `past date fails`() = runTest {
            val pastDate = LocalDate.now().minusDays(10)
            val error = FutureValidator.runValidation(pastDate, constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.FUTURE_VIOLATION, error?.code)
        }

        @Test fun `future datetime is valid`() = runTest {
            val futureDateTime = LocalDateTime.now().plusHours(1)
            assertNull(FutureValidator.runValidation(futureDateTime, constraint(), ctx))
        }

        @Test fun `past datetime fails`() = runTest {
            val pastDateTime = LocalDateTime.now().minusHours(1)
            val error = FutureValidator.runValidation(pastDateTime, constraint(), ctx)
            assertNotNull(error)
        }

        @Test fun `future datetime beyond within range fails`() = runTest {
            val futureDateTime = LocalDateTime.now().plusDays(100)
            val error = FutureValidator.runValidation(futureDateTime, constraint(withinDays = 30), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.FUTURE_VIOLATION, error?.code)
        }

        @Test fun `future datetime within range is valid`() = runTest {
            val futureDateTime = LocalDateTime.now().plusDays(5)
            assertNull(FutureValidator.runValidation(futureDateTime, constraint(withinDays = 30), ctx))
        }
    }

    @Nested
    inner class PastValidatorTest {
        private fun constraint(
            withinDays: Long = 0, withinHours: Long = 0, withinMinutes: Long = 0,
            withinYears: Long = 0, withinMonths: Long = 0, withinWeeks: Long = 0, withinSeconds: Long = 0
        ) = PastConstraint(
            withinSeconds = withinSeconds, withinMinutes = withinMinutes, withinHours = withinHours,
            withinDays = withinDays, withinWeeks = withinWeeks, withinMonths = withinMonths, withinYears = withinYears,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(PastValidator.runValidation(null, constraint(), ctx))
        }

        @Test fun `past date is valid`() = runTest {
            val pastDate = LocalDate.now().minusDays(10)
            assertNull(PastValidator.runValidation(pastDate, constraint(), ctx))
        }

        @Test fun `future date fails`() = runTest {
            val futureDate = LocalDate.now().plusDays(10)
            val error = PastValidator.runValidation(futureDate, constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.PAST_VIOLATION, error?.code)
        }

        @Test fun `past datetime beyond within range fails`() = runTest {
            val oldDateTime = LocalDateTime.now().minusDays(100)
            val error = PastValidator.runValidation(oldDateTime, constraint(withinDays = 30), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.PAST_VIOLATION, error?.code)
        }

        @Test fun `past datetime within range is valid`() = runTest {
            val recentDateTime = LocalDateTime.now().minusDays(5)
            assertNull(PastValidator.runValidation(recentDateTime, constraint(withinDays = 30), ctx))
        }
    }

    @Nested
    inner class TemporalMinValidatorTest {
        private fun constraint(value: String, inclusive: Boolean = true) = TemporalMinConstraint(
            value = value, inclusive = inclusive,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(TemporalMinValidator.runValidation(null, constraint("2024-01-01"), ctx))
        }

        @Test fun `date after min is valid`() = runTest {
            assertNull(TemporalMinValidator.runValidation(LocalDate.of(2024, 6, 1), constraint("2024-01-01"), ctx))
        }

        @Test fun `date at min inclusive is valid`() = runTest {
            assertNull(TemporalMinValidator.runValidation(LocalDate.of(2024, 1, 1), constraint("2024-01-01", inclusive = true), ctx))
        }

        @Test fun `date at min exclusive fails`() = runTest {
            val error = TemporalMinValidator.runValidation(LocalDate.of(2024, 1, 1), constraint("2024-01-01", inclusive = false), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.MIN_VALUE_VIOLATION, error?.code)
        }

        @Test fun `date before min fails`() = runTest {
            val error = TemporalMinValidator.runValidation(LocalDate.of(2023, 6, 1), constraint("2024-01-01"), ctx)
            assertNotNull(error)
        }
    }

    @Nested
    inner class TemporalMaxValidatorTest {
        private fun constraint(value: String, inclusive: Boolean = true) = TemporalMaxConstraint(
            value = value, inclusive = inclusive,
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(TemporalMaxValidator.runValidation(null, constraint("2025-12-31"), ctx))
        }

        @Test fun `date before max is valid`() = runTest {
            assertNull(TemporalMaxValidator.runValidation(LocalDate.of(2025, 6, 1), constraint("2025-12-31"), ctx))
        }

        @Test fun `date at max inclusive is valid`() = runTest {
            assertNull(TemporalMaxValidator.runValidation(LocalDate.of(2025, 12, 31), constraint("2025-12-31", inclusive = true), ctx))
        }

        @Test fun `date at max exclusive fails`() = runTest {
            val error = TemporalMaxValidator.runValidation(LocalDate.of(2025, 12, 31), constraint("2025-12-31", inclusive = false), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.MAX_VALUE_VIOLATION, error?.code)
        }

        @Test fun `date after max fails`() = runTest {
            val error = TemporalMaxValidator.runValidation(LocalDate.of(2026, 6, 1), constraint("2025-12-31"), ctx)
            assertNotNull(error)
        }
    }

    @Nested
    inner class AllowedDaysValidatorTest {
        private fun constraint(vararg days: DayOfWeek) = AllowedDaysConstraint(
            days = days.toSet(),
            groups = setOf(OnDefault::class), message = ""
        )

        @Test fun `null is valid`() = runTest {
            assertNull(AllowedDaysValidator.runValidation(null, constraint(DayOfWeek.MONDAY), ctx))
        }

        @Test fun `allowed day is valid`() = runTest {
            val monday = LocalDate.of(2024, 1, 1)
            assertNull(AllowedDaysValidator.runValidation(monday, constraint(DayOfWeek.MONDAY), ctx))
        }

        @Test fun `disallowed day fails`() = runTest {
            val monday = LocalDate.of(2024, 1, 1)
            val error = AllowedDaysValidator.runValidation(monday, constraint(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.DAY_OF_WEEK_VIOLATION, error?.code)
        }

        @Test fun `LocalTime is skipped`() = runTest {
            assertNull(AllowedDaysValidator.runValidation(LocalTime.of(10, 0), constraint(DayOfWeek.MONDAY), ctx))
        }

        @Test fun `LocalDateTime extracts day correctly`() = runTest {
            val mondayTime = LocalDateTime.of(2024, 1, 1, 10, 30)
            assertNull(AllowedDaysValidator.runValidation(mondayTime, constraint(DayOfWeek.MONDAY), ctx))
        }
    }
}
