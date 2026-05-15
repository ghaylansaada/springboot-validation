package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctConstraint
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that all elements in the annotated collection or array are unique.
 * Uniqueness can be checked on entire element values (default) or on specific top-level fields via [by].
 * Repeatable to allow multiple independent uniqueness rules on the same collection.
 *
 * @property by Top-level field names to compare; if empty, whole-element equality is used.
 * @property mode [DistinctMode.PER_FIELD] checks each field independently; [DistinctMode.COMBINATION] requires the combined tuple to be unique.
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = DistinctConstraint::class, validatedBy = [DistinctValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Distinct(
	val by: Array<String> = [],
	val mode: DistinctMode = DistinctMode.PER_FIELD,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
) {
	
	/** Strategy for multi-field uniqueness checks. */
	enum class DistinctMode {
		/** The combination of all [by] fields must be unique across elements. */
		COMBINATION,
		/** Each field in [by] must be unique independently. */
		PER_FIELD
	}
}