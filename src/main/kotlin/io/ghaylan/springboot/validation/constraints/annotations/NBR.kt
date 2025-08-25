package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.nbr.NBRConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.nbr.NBRValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence represents a valid National Business Register (NBR) ID.
 *
 * ### Description
 * The `@NBR` annotation ensures that the annotated value (or each element in a collection)
 * conforms to the format and checksum rules of the National Business Register number.
 * This is useful for validating business identifiers used in financial, legal, or administrative contexts.
 *
 * The validation logic checks that the value:
 * - Has a length between 2 and 8 characters.
 * - Is padded to 8 characters with leading zeros for validation.
 * - Matches the pattern of 7 digits followed by a checksum letter from the set `[A-H, J-N, P, Q, R, S, T, V, W, X, Y, Z]`.
 * - Has a correct checksum letter based on a weighted sum modulo 23.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation fails if the value does not match the required pattern or checksum.
 * - For arrays or collections, each element must individually be a valid NBR.
 *
 * ### Example Usage
 * ```kotlin
 * @NBR
 * val businessId: String
 * // ✅ businessId must be a valid National Business Register number
 *
 * -----
 *
 * @NBR
 * val ids: List<CharSequence>
 * // ✅ each value in ids must be a valid National Business Register number
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.NBR_FORMAT_VIOLATION]
 *   Triggered when a value does not conform to the NBR format or checksum.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = NBRConstraint::class, validatedBy = [NBRValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NBR(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])