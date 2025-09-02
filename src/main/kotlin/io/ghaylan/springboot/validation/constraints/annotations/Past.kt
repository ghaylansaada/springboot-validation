package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.temporal.past.PastConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.past.PastValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value represents a point strictly in the past,
 * relative to the current system time at validation.
 *
 * ### Description
 * The `@Past` annotation ensures that the annotated value (or each element in a collection), which must implement
 * [java.time.temporal.Temporal], occurs before the current time at validation.
 * This is commonly used to validate timestamps such as creation dates, event times,
 * or any temporal data that must be earlier than now.
 *
 * Additionally, it supports specifying a time window within which the value must fall.
 * For example, you can require the value to be within the past 5 days or 2 hours.
 * The window is specified using the `withinSeconds`, `withinMinutes`, `withinHours`,
 * `withinDays`, `withinWeeks`, `withinMonths`, and `withinYears` properties.
 * When one or more window properties are set, the validated temporal must be within that
 * duration counted backwards from the current time.
 *
 * ### Supported Types
 * - Any implementation of [java.time.temporal.Temporal]:
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
 * - The value must be strictly before the current system time.
 * - If any `within*` property is set, the value must be after (now minus the specified window).
 *   For example, with `withinDays = 5`, the value must be within the last 5 days.
 * - For arrays or collections, each element must individually be in the past.
 *
 * ### Example Usage
 * ```kotlin
 * @Past
 * val createdAt: LocalDateTime
 * // ✅ createdAt must be strictly before now
 *
 * -----
 *
 * @Past(withinDays = 7)
 * val lastLogins: List<ZonedDateTime>
 * // ✅ each date in lastLogins must be within the last 7 days
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.PAST_VIOLATION]
 *   Triggered when the value (or any element in a list/array) is not strictly in the past or outside the specified time window.
 *
 * @property withinSeconds Validates the temporal value to be within this number of seconds in the past.
 * @property withinMinutes Validates the temporal value to be within this number of minutes in the past.
 * @property withinHours Validates the temporal value to be within this number of hours in the past.
 * @property withinDays Validates the temporal value to be within this number of days in the past.
 * @property withinWeeks Validates the temporal value to be within this number of weeks in the past.
 * @property withinMonths Validates the temporal value to be within this number of months in the past.
 * @property withinYears Validates the temporal value to be within this number of years in the past.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = PastConstraint::class, validatedBy = [PastValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Past(
    val withinSeconds : Long = 0,
    val withinMinutes : Long = 0,
    val withinHours : Long = 0,
    val withinDays : Long = 0,
    val withinWeeks : Long = 0,
    val withinMonths : Long = 0,
    val withinYears : Long = 0,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])