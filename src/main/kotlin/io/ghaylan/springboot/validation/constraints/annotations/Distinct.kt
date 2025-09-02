package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctConstraint
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that all elements in the annotated collection or array are unique,
 * either by their entire value or by specified top-level fields.
 *
 * ### Description
 * The `@Distinct` annotation ensures that the annotated collection or array contains
 * no duplicate elements. It supports validation at the whole element level or by
 * one or more specified fields within elements. This is useful for enforcing
 * uniqueness constraints on collections of scalar values, objects, or maps,
 * such as unique identifiers, emails, or composite keys within lists of entities.
 *
 * It also supports field-level uniqueness within nested collections, allowing
 * uniqueness constraints on individual fields inside objects contained in the collection.
 *
 * The annotation supports two distinct validation modes for multi-field uniqueness:
 * - `PER_FIELD` — each specified field must be unique independently.
 * - `COMBINATION` — the combination of all specified fields must be unique together.
 *
 * ### Supported Types
 * - Collections or arrays of scalar types (e.g., `List<Int>`, `Array<String>`).
 * - Collections or arrays of objects (e.g., `List<User>`, `Array<Map<String, Any>>`).
 * - Fields within objects inside collections (e.g., `email: String` in `List<User>`).
 *
 * ### Validation Rules
 * - If [by] is empty:
 *      - The entire elements are compared using their `equals()` method.
 * - If [by] is not empty:
 *      - Only top-level fields are supported (nested fields are not allowed).
 *      - In `PER_FIELD` mode, each field is validated independently for uniqueness.
 *      - In `COMBINATION` mode, the combined values of all specified fields must be unique.
 * - Collections or arrays with zero or one element are always valid.
 * - Multiple `null` elements or `null` field values are considered duplicates.
 * - Nested or indexed fields like `"address.city"` are not supported.
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, allowing multiple distinct uniqueness constraints
 * on the same collection for different field sets or modes.
 *
 * ### Example Usage
 * ```kotlin
 * // Example 1: Scalar collection uniqueness
 * @Distinct
 * val ratings: List<Int>
 * // ✅ Valid: listOf(1, 2, 3)
 * // ❌ Invalid: listOf(1, 2, 1)
 *
 * -----
 *
 * // Example 2: Object-level uniqueness by single field
 * @Distinct(by = ["email"])
 * val members: List<User>
 * // ✅ Valid: listOf(User("a@x.com"), User("b@x.com"))
 * // ❌ Invalid: listOf(User("a@x.com"), User("a@x.com"))
 *
 * -----
 *
 * // Example 3: Field-level uniqueness inside nested list
 * data class User(
 *     @Distinct
 *     val email: String)
 *
 * val users: List<User>
 * // Ensures each email is unique across the users list.
 * // For invalid input:
 * //   users[0].email = "a@x.com"
 * //   users[1].email = "a@x.com"
 * // ❌ Fails validation due to duplicate email values
 *
 * -----
 *
 * // Example 4: Combination mode with multiple fields
 * @Distinct(by = ["id", "category"], mode = Distinct.Mode.COMBINATION)
 * val products: List<Product>
 * // ✅ Valid if combination of id and category is unique across elements.
 * // ❌ Invalid if two elements share the same id and category.
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.DISTINCT_VALUE_VIOLATION]
 *   Triggered when duplicate elements or field values are detected.
 *
 * @property by Optional array of top-level field names to compare for uniqueness.
 *              If empty, uniqueness is checked on entire element values.
 * @property mode Defines the strategy for multi-field uniqueness validation.
 *                Defaults to [DistinctMode.PER_FIELD].
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = DistinctConstraint::class, validatedBy = [DistinctValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Distinct(
    val by : Array<String> = [],
    val mode : DistinctMode = DistinctMode.PER_FIELD,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])
{
    /**
     * Specifies the strategy used by the `@Distinct` annotation to validate uniqueness
     * when targeting specific fields of collection elements.
     */
    enum class DistinctMode
    {
        /**
         * Validates that the **combination** of all fields listed in the `by` parameter is unique.
         *
         * Example: if `by = ["email", "username"]`, the pair of (`email`, `username`) must be unique
         * across all elements in the collection.
         */
        COMBINATION,

        /**
         * Validates that each field listed in the `by` parameter is unique independently.
         *
         * Example: if `by = ["email", "username"]`, then all `email` values must be distinct,
         * and all `username` values must be distinct — separately.
         */
        PER_FIELD
    }
}