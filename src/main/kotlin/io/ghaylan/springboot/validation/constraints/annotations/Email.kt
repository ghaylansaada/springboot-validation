package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.email.EmailConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.email.EmailValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated value represents a syntactically valid email address
 * (or each element in a collection/array is valid).
 *
 * ### Description
 * The `@Email` annotation ensures that the annotated value (or each element in a collection)
 * conforms to a standard email address format.
 * This includes verifying a valid local part, the presence of exactly one `@` symbol,
 * a proper domain name, and a valid top-level domain (TLD) of 1–6 alphabetic characters.
 * It is commonly applied to user contact fields, account identifiers, or mailing list entries.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - The email must contain exactly one `@` symbol.
 * - Local part (before `@`) may include letters, digits, dots (`.`), hyphens (`-`), and underscores (`_`).
 * - Domain part (after `@`) must be a valid domain name (e.g., `example.com`).
 * - The domain must end with a valid TLD of **1–6 alphabetic characters**.
 * - For arrays or collections, each element must individually meet the above rules.
 *
 * ### Example Usage
 * ```kotlin
 * @Email
 * val primaryEmail: String
 * // ✅ Must be a valid email address
 *
 * -----
 *
 * @Email
 * val contactEmails: List<String>
 * // ✅ Each email in the list must be valid
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.EMAIL_FORMAT_VIOLATION]
 *   Triggered when the value does not match the required email format.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = EmailConstraint::class, validatedBy = [EmailValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Email(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])