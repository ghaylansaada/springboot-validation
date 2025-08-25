package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.array.size.ArraySizeConstraint
import io.ghaylan.springboot.validation.constraints.validators.array.size.ArraySizeValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated array or collection has a size within a defined inclusive range.
 *
 * ### Description
 * The `@ArraySize` annotation ensures that the size (i.e., number of elements) of the annotated array or collection
 * is within the specified `[min, max]` range, inclusive. This is commonly used to validate that a list or array
 * has the expected number of items, such as enforcing minimum or maximum selection counts in forms.
 *
 * ### Supported Types
 * - `Array<*>`
 * - Kotlin primitive arrays: `IntArray`, `LongArray`, `DoubleArray`, etc.
 * - `Collection<*>` types: `List`, `Set`, etc.
 *
 * ### Validation Rules
 * - If the value is `null`, no error is returned (null-safe).
 * - Validation is triggered only if the value is an array or a supported collection type.
 * - The value must have a size between `min` and `max` (inclusive).
 *
 * ### Example Usage
 * ```kotlin
 * @ArraySize(min = 1, max = 5)
 * val selectedOptions: List<String>
 * // ✅ Ensures selectedOptions list has between 1 and 5 elements
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.ARRAY_SIZE_VIOLATION]
 *   Triggered when the size of the array or collection is smaller than `min` or greater than `max`.
 *
 * @property min Minimum number of elements allowed (inclusive). Default is `0`.
 * @property max Maximum number of elements allowed (inclusive). Default is `Int.MAX_VALUE`.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = ArraySizeConstraint::class, validatedBy = [ArraySizeValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ArraySize(
    val min : Int = 0,
    val max : Int = Int.MAX_VALUE,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])