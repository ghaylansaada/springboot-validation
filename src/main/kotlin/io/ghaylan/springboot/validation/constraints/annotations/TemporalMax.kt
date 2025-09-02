package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.temporal.max.TemporalMaxConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.max.TemporalMaxValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value is before (or equal to) a specified maximum point in time.
 *
 * ### Description
 * The `@TemporalMax` annotation ensures that the annotated temporal value (or each element in a collection) does **not exceed** a specified date/time value.
 * This is useful when validating that a date/time input remains within a maximum allowed boundary (e.g., scheduled dates, birth dates, time limits).
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
 * - The `inclusive` parameter determines whether the annotated value can be **equal to** the maximum:
 *   - `inclusive = true`: value **≤** max
 *   - `inclusive = false`: value **<** max
 * - Types must match exactly: for example, comparing `LocalDateTime` with `LocalDate` will fail at runtime.
 * - For collection or array types, each element must individually satisfy the maximum temporal constraint.
 *
 * ### Example Usage
 * ```kotlin
 * @TemporalMax("2025-12-31", inclusive = true)
 * val endDate: LocalDate
 * // ✅ Ensures endDate is not after 2025-12-31
 *
 * -----
 *
 * @TemporalMax("22-00-00", inclusive = true)
 * val closeTimes: List<LocalTime>
 * // ✅ Ensures all closeTimes are on or before 22-00-00
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.MAX_VALUE_VIOLATION]
 *   Triggered when the annotated value (or any element in a list/array) is after the maximum allowed value.
 *      - Inclusive: _"must be before or equal to `X`"_
 *      - Exclusive: _"must be before `X`"_
 *
 * @property value ISO-8601 date/time string that defines the maximum allowed value (must match the temporal type of the field).
 * @property inclusive Whether the value is allowed to be equal to the specified maximum.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = TemporalMaxConstraint::class, validatedBy = [TemporalMaxValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class TemporalMax(
    val value : String,
    val inclusive : Boolean,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])