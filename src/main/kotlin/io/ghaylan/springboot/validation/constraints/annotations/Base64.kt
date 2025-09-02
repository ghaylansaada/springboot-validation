package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.base64.Base64Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.base64.Base64Validator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence is a valid Base64-encoded string.
 *
 * ### Description
 * The `@Base64` annotation ensures that the annotated value (or each element in a collection)
 * contains only valid Base64 characters and can be successfully decoded using
 * the standard [java.util.Base64] decoder.
 * This is useful for validating encoded binary data such as images, files, or tokens
 * transmitted as text in JSON or XML payloads.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The value must contain only characters in the Base64 alphabet:
 *   `A–Z`, `a–z`, `0–9`, `+`, `/`, and optionally `=` as padding.
 * - The length must be a multiple of 4.
 * - The value must be decodable by `Base64.getDecoder().decode(...)` without error.
 * - For arrays or collections, each element must individually meet the above rules.
 *
 * ### Example Usage
 * ```kotlin
 * @Base64
 * val encodedData: String
 * // ✅ Must be valid Base64 (e.g., "SGVsbG8gV29ybGQh")
 *
 * -----
 *
 * @Base64
 * val imageDataList: List<CharSequence>
 * // ✅ Each string must be a valid Base64-encoded value
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.BASE64_VIOLATION]
 *   Triggered when the value contains invalid characters, incorrect padding, length not divisible by 4,
 *   or fails to decode successfully.
 *
 * @property Base64 Applies validation to ensure the annotated string or sequence represents a valid Base64-encoded value.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = Base64Constraint::class, validatedBy = [Base64Validator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Base64(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])