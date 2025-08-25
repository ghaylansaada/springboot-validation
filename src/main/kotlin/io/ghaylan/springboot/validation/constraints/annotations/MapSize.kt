package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.map.MapSizeConstraint
import io.ghaylan.springboot.validation.constraints.validators.map.MapSizeValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the number of key-value pairs in a map is within the specified bounds.
 *
 * ### Description
 * The `@MapSize` annotation ensures that a `Map` has a number of entries (size) that falls within the inclusive range defined by `min` and `max`.
 * This annotation is useful for enforcing business rules where a minimum or maximum number of key-value pairs is expected, such as metadata maps, structured properties, or configurations.
 *
 * It can also be applied to **arrays** or **collections** of maps, where each map instance in the collection will be validated independently.
 *
 * ### Supported Types
 * - `Map<*, *>`
 * - Arrays, Lists, or other collections containing `Map` instances
 *
 * ### Validation Rules
 * - The map must contain **at least `min` entries** and **at most `max` entries**.
 * - `min` must be a non-negative integer.
 * - `max` must be greater than or equal to `min`.
 * - Validation is **skipped** for `null` values. (Nullability should be handled separately if needed.)
 *
 * ### Example Usage
 * ```kotlin
 * @MapSize(min = 2, max = 5)
 * val attributes: Map<String, String>
 * // ✅ Single map with size between 2 and 5
 *
 * -----
 *
 * @MapSize(min = 1, max = 3)
 * val listOfMaps: List<Map<String, Any>>
 * // ✅ List of maps, each requiring between 1 and 3 entries
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.OBJECT_SIZE_VIOLATION]
 *   Triggered when the number of entries in the map is less than `min` or greater than `max`.
 *
 * @property min Minimum number of key-value pairs required in the map. Default is `0`.
 * @property max Maximum number of key-value pairs allowed in the map. Default is `Int.MAX_VALUE`.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = MapSizeConstraint::class, validatedBy = [MapSizeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class MapSize(
    val min : Int = 0,
    val max : Int = Int.MAX_VALUE,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])