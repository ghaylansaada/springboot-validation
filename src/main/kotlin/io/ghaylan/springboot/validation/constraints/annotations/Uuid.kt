package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.uuid.UuidConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.uuid.UuidValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence represents a valid UUID string.
 *
 * ### Description
 * The `@Uuid` annotation ensures that the annotated value (or each element in a collection)
 * conforms to the standard UUID format defined by [java.util.UUID].
 * This is particularly useful for validating identifier fields such as entity IDs,
 * correlation tokens, or keys received from external sources.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation fails if the value cannot be parsed by `UUID.fromString(...)`.
 * - For arrays or collections, each element must individually represent a valid UUID.
 *
 * ### Example Usage
 * ```kotlin
 * @Uuid
 * val userId: String
 * // ✅ userId must be a valid UUID
 *
 * -----
 *
 * @Uuid
 * val deviceIds: List<CharSequence>
 * // ✅ each value in deviceIds must be a valid UUID
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.UUID_PATTERN_VIOLATION]
 *   Triggered when a value does not conform to the UUID format.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = UuidConstraint::class, validatedBy = [UuidValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Uuid(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])