package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.number.latitude.LatitudeConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.latitude.LatitudeValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated decimal value represents a valid **latitude** coordinate.
 *
 * ### Description
 * The `@Latitude` annotation ensures that the annotated value is a valid latitude, i.e., a `Double` value
 * within the geographic bounds of **-90.0 to 90.0 degrees**, inclusive. This is commonly used for validating
 * location-based data such as coordinates in geospatial models or user-provided positions.
 *
 * ### Supported Types
 * - `Double`
 *
 * ### Validation Rules
 * - The annotated value must be between **-90.0** and **90.0**, inclusive.
 *
 * ### Example Usage
 * ```kotlin
 * @Latitude
 * val lat: Double
 * // ✅ Ensures `lat` is within [-90.0, 90.0]
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.LATITUDE_VIOLATION]
 *   Triggered when the annotated value is outside the allowed latitude range.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = LatitudeConstraint::class, validatedBy = [LatitudeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Latitude(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])