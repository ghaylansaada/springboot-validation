package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.temporal.future.FutureConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.future.FutureValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value represents a point strictly in the future,
 * relative to the current system time at validation.
 *
 * ### Description
 * The `@Future` annotation ensures that the annotated value (or each element in a collection), which must implement
 * [java.time.temporal.Temporal], occurs after the current time at validation.
 * This is commonly used to validate timestamps such as due dates, expiration times,
 * scheduled events, or any temporal data that must be later than now.
 *
 * Additionally, it supports specifying a time window within which the value must fall.
 * For example, you can require the value to be within the next 5 days or 2 hours.
 * The window is specified using the `withinSeconds`, `withinMinutes`, `withinHours`,
 * `withinDays`, `withinWeeks`, `withinMonths`, and `withinYears` properties.
 * When one or more window properties are set, the validated temporal must be within that
 * duration counted forward from the current time.
 *
 * ### Supported Types
 * - Any implementation of [java.time.temporal.Temporal], including:
 *   - [java.time.LocalTime]
 *   - [java.time.OffsetTime]
 *   - [java.time.LocalDate]
 *   - [java.time.LocalDateTime]
 *   - [java.time.OffsetDateTime]
 *   - [java.time.ZonedDateTime]
 *   - [java.time.Instant]
 * - Arrays or collections of the above types (each element is validated individually).
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The value must be strictly after the current system time.
 * - If any `within*` property is set, the value must be before (now plus the specified window).
 *   For example, with `withinDays = 5`, the value must be within the next 5 days.
 * - For arrays or collections, each element must individually be in the future.
 *
 * ### Example Usage
 * ```kotlin
 * @Future
 * val appointment: LocalDateTime
 * // ✅ appointment must be strictly after now
 *
 * -----
 *
 * @Future(withinDays = 7)
 * val scheduledDates: List<LocalDateTime>
 * // ✅ each date in scheduledDate must be within the next 7 days
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.FUTURE_VIOLATION]
 *   Triggered when the value (or any element in a list/array) is not strictly in the future or outside the specified time window.
 *
 * @property withinSeconds Validates the temporal value to be within this number of seconds in the future.
 * @property withinMinutes Validates the temporal value to be within this number of minutes in the future.
 * @property withinHours Validates the temporal value to be within this number of hours in the future.
 * @property withinDays Validates the temporal value to be within this number of days in the future.
 * @property withinWeeks Validates the temporal value to be within this number of weeks in the future.
 * @property withinMonths Validates the temporal value to be within this number of months in the future.
 * @property withinYears Validates the temporal value to be within this number of years in the future.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = FutureConstraint::class, validatedBy = [FutureValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Future(
    val withinSeconds : Long = 0,
    val withinMinutes : Long = 0,
    val withinHours : Long = 0,
    val withinDays : Long = 0,
    val withinWeeks : Long = 0,
    val withinMonths : Long = 0,
    val withinYears : Long = 0,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])