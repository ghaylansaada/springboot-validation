package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredConstraint
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is present (non-null and non-empty), either unconditionally
 * or conditionally based on whether a sibling field is null or non-null. Repeatable.
 *
 * @property dependentField Sibling field whose null/non-null state controls this field's requirement.
 *                          Ignored when [condition] is [RequirementCondition.ALWAYS].
 * @property condition When to enforce presence; default is [RequirementCondition.ALWAYS].
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = RequiredConstraint::class, validatedBy = [RequiredValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Required(
	val dependentField: String = "",
	val condition: RequirementCondition = RequirementCondition.ALWAYS,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
) {
	
	/** Controls when the annotated field is treated as required. */
	enum class RequirementCondition {
		/** Always required. */
		ALWAYS,
		/** Required only when [dependentField] is `null`. */
		IF_DEPENDENT_NULL,
		/** Required only when [dependentField] is non-null. */
		IF_DEPENDENT_NOT_NULL,
	}
}