package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays.AllowedDaysConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays.AllowedDaysValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import java.time.DayOfWeek
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value occurs on one of the specified allowed days of the week.
 *
 * ### Description
 * The `@AllowedDays` annotation ensures that the annotated temporal value (or each element in a collection)
 * represents a day of the week included in the specified allowed days.
 * This is useful to restrict date/time inputs to particular weekdays, such as business days,
 * weekends, or custom schedules.
 *
 * Validation is skipped for temporal types that do not represent a date with a day of week,
 * such as `LocalTime` or `OffsetTime`.
 *
 * ### Supported Types
 * - Any implementation of [java.time.temporal.Temporal]:
 *   - [java.time.LocalDate]
 *   - [java.time.LocalDateTime]
 *   - [java.time.OffsetDateTime]
 *   - [java.time.ZonedDateTime]
 *   - [java.time.Instant]
 * - Arrays or collections of the above types (each element is validated individually).
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation is skipped if the value is a `LocalTime` or `OffsetTime` (time-only values).
 * - Validation fails if the day of week of the value is not included in the specified allowed days.
 * - For arrays or collections, each element must individually fall on an allowed day.
 *
 * ### Example Usage
 * ```kotlin
 * @AllowedDays(days = [DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY])
 * val meetingDate: LocalDate
 * // ✅ meetingDate must be Monday, Wednesday, or Friday
 *
 * -----
 *
 * @AllowedDays(days = [DayOfWeek.SATURDAY, DayOfWeek.SUNDAY])
 * val weekendDates: List<LocalDate>
 * // ✅ each date in weekendDates must be Saturday or Sunday
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.DAY_OF_WEEK_VIOLATION]
 *   Triggered when the temporal value's day of week (or any element in a list/array) is not in the allowed days.
 *
 * @property days The set of allowed `DayOfWeek` values on which the temporal value must fall.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = AllowedDaysConstraint::class, validatedBy = [AllowedDaysValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class AllowedDays(
    val days: Array<DayOfWeek>,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])