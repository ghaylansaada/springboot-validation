package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.hexcolor.HexColorConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.hexcolor.HexColorValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence represents a valid hexadecimal color code.
 *
 * ### Description
 * The `@HexColor` annotation ensures that the annotated value (or each element in a collection)
 * matches the standard CSS hex color format. This can be used to validate color-related fields,
 * such as theme colors, style configurations, or design palette inputs.
 *
 * Supported formats:
 * - `#RRGGBB` — six hexadecimal digits representing red, green, and blue components
 * - `#RGB` — shorthand format with three hexadecimal digits (expanded internally)
 *
 * Both uppercase and lowercase hexadecimal digits are accepted.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation fails if:
 *   - The value does not start with `#`
 *   - The value is not exactly 4 or 7 characters long
 *   - The characters after `#` are not valid hexadecimal digits (`0-9`, `A-F`, `a-f`)
 * - For arrays or collections, each element must individually match the format.
 *
 * ### Example Usage
 * ```kotlin
 * @HexColor
 * val primaryColor: String
 * // ✅ primaryColor must be a valid hex color string like "#FF5733" or "#0F0"
 *
 * -----
 *
 * @HexColor
 * val themeColors: List<CharSequence>
 * // ✅ each value in themeColors must be a valid hex color
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.HEX_COLOR_CODE_FORMAT_VIOLATION]
 *   Triggered when a value does not match the required hex color format.
 *
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = HexColorConstraint::class, validatedBy = [HexColorValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class HexColor(
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])