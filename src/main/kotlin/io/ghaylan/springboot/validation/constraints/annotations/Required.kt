package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredConstraint
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is present (non-null and non-empty), either always or conditionally based on the state of other sibling field(s).
 *
 * ### Description
 * The `@Required` annotation enforces that the annotated field or parameter must not be null, blank, or empty.
 * It can be used unconditionally or conditionally depending on the value(s) of other **sibling** field(s) within the same object.
 * This supports use cases where a field's presence depends on the presence or absence of other related fields,
 * such as fallback or alternative fields in forms or DTOs.
 *
 * ### Supported Types
 * - All nullable types.
 * - Common usages include `String?`, `List<T>?`, `Map<K, V>?`, arrays, and other container types.
 * - Deep null and emptiness checks are performed via custom logic (`isDeepNullOrEmpty()`), covering nested collections and strings.
 *
 * ### Validation Rules
 * - If `condition = ALWAYS`, the field is always required (non-null and non-empty).
 * - If `condition = IF_DEPENDENT_NULL`, the field is required only if the sibling field specified in `dependentField` is `null`.
 * - If `condition = IF_DEPENDENT_NOT_NULL`, the field is required only if the sibling field specified in `dependentField` is **not null**.
 * - Empty strings (`""`) and empty collections are treated as missing and cause validation failure.
 * - The `dependentField` must be a sibling field/parameter of the annotated field within the same object; otherwise, conditional logic is ignored.
 *
 * ### Repeatability
 * This annotation is `@Repeatable`, enabling multiple `@Required` constraints with different dependencies or conditions on the same field.
 *
 * ### Example Usage
 * ```kotlin
 * @Required
 * val email: String?
 * // ✅ email must not be null or blank
 *
 * -----
 *
 * @Required(
 *     dependentField = "email",
 *     condition = Required.RequirementCondition.IF_DEPENDENT_NULL)
 * val fallbackContact: String?
 * // ✅ fallbackContact is required if email is null
 * // ✅ If email is not null, fallbackContact is optional
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.REQUIRED_VIOLATION]
 *   - Triggered when the value is missing and required either unconditionally or conditionally based on dependent field state.
 *
 * @property dependentField Name of the sibling field/parameter whose presence or absence controls this field's requirement. Ignored if `condition` is `ALWAYS`.
 * @property condition The condition that determines when this field is required.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = RequiredConstraint::class, validatedBy = [RequiredValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Required(
    val dependentField : String = "",
    val condition : RequirementCondition = RequirementCondition.ALWAYS,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])
{
    /**
     * Used within the `@Required` annotation to determine whether the annotated value should be validated
     * based on the state of other sibling fields or always.
     */
    enum class RequirementCondition
    {
        /** The annotated field is always required, regardless of any other fields. */
        ALWAYS,

        /** The annotated field is required **only if `dependentField` is `null`. */
        IF_DEPENDENT_NULL,

        /** The annotated field is required **only `dependentField` is **not null**. */
        IF_DEPENDENT_NOT_NULL,
    }
}