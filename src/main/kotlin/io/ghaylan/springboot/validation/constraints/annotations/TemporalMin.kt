package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.temporal.min.TemporalMinConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.min.TemporalMinValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value is after (or equal to) a specified minimum point in time.
 *
 * ### Description
 * The `@TemporalMin` annotation ensures that the annotated temporal value (or each element in a collection) does **not fall below** a specified date/time value.
 * This is useful when validating that a date/time input remains within a minimum allowed boundary (e.g., start dates, expiration dates, activation times).
 *
 * ### Supported Types
 * - Any type implementing `java.time.temporal.Temporal`, including:
 *      - `LocalDate` → Format: "HH:mm:ss" (e.g., "14:30:00")
 *      - `LocalTime` → Format: "HH:mm:ssXXX" (e.g., "14:30:00+02:00")
 *      - `LocalDateTime` → Format: "yyyy-MM-dd" (e.g., "2025-08-12")
 *      - `OffsetTime` → Format: "yyyy-MM-dd'T'HH:mm:ss" (e.g., "2025-08-12T14:30:00")
 *      - `OffsetDateTime` → Format: "yyyy-MM-dd'T'HH:mm:ssXXX" (e.g., "2025-08-12T14:30:00+02:00")
 *      - `ZonedDateTime` → Format: "yyyy-MM-dd'T'HH:mm:ssz" (e.g., "2025-08-12T14:30:00Europe/Paris")
 *      - `Instant` → Format: "yyyy-MM-dd'T'HH:mm:ss'Z'" (UTC instant, e.g., "2025-08-12T12:30:00Z")
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - The `value` parameter must be a valid ISO-8601 date/time string compatible with the annotated type.
 * - The `inclusive` parameter determines whether the annotated value can be **equal to** the minimum:
 *   - `inclusive = true`: value **≥** min
 *   - `inclusive = false`: value **>** min
 * - Types must match exactly: for example, comparing `LocalDateTime` with `LocalDate` will fail at runtime.
 * - For collection or array types, each element must individually satisfy the minimum temporal constraint.
 *
 * ### Example Usage
 * ```kotlin
 * @TemporalMin("2023-01-01", inclusive = true)
 * val startDate: LocalDate
 * // ✅ Ensures startDate is not before 2023-01-01
 *
 * -----
 *
 * @TemporalMin("08-00-00", inclusive = true)
 * val activationTimes: List<LocalTime>
 * // ✅ Ensures all activationTimes are on or after 08-00-00
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.MIN_VALUE_VIOLATION]
 *   Triggered when the annotated value (or any element in a list/array) is before the minimum allowed value.
 *      - Inclusive: _"must be after or equal to `X`"_
 *      - Exclusive: _"must be after `X`"_
 *
 * @property value ISO-8601 date/time string that defines the minimum allowed value (must match the temporal type of the field).
 * @property inclusive Whether the value is allowed to be equal to the specified minimum.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = TemporalMinConstraint::class, validatedBy = [TemporalMinValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class TemporalMin(
    val value : String,
    val inclusive : Boolean = true,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])