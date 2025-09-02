package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.number.longitude.LongitudeConstraint
import io.ghaylan.springboot.validation.constraints.validators.number.longitude.LongitudeValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated decimal value represents a valid **longitude** coordinate.
 *
 * ### Description
 * The `@Longitude` annotation ensures that the annotated value is a valid longitude, i.e., a `Double` value
 * within the geographic bounds of **-180.0 to 180.0 degrees**, inclusive. This is commonly used for validating
 * location-based data such as coordinates in geospatial models or user-provided positions.
 *
 * ### Supported Types
 * - `Double`
 *
 * ### Validation Rules
 * - The annotated value must be between **-180.0** and **180.0**, inclusive.
 *
 * ### Example Usage
 * ```kotlin
 * @Longitude
 * val lng: Double
 * // ✅ Ensures `lng` is within [-180.0, 180.0]
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.LONGITUDE_VIOLATION]
 *   Triggered when the annotated value is outside the allowed longitude range.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = LongitudeConstraint::class, validatedBy = [LongitudeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Longitude(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])