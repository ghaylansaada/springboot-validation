package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.contains.StrOccConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.contains.StrOccValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated string matches [value] according to [mode] and that the occurrence
 * count falls within `[minOccurrences, maxOccurrences]`. Repeatable.
 *
 * @property value The required substring.
 * @property minOccurrences Minimum occurrence count (inclusive, default 1).
 * @property maxOccurrences Maximum occurrence count (inclusive, default [Int.MAX_VALUE]).
 * @property ignoreCase Whether the match is case-insensitive.
 * @property mode The matching strategy (EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH).
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = StrOccConstraint::class, validatedBy = [StrOccValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class StrOcc(
	val value: String,
	val minOccurrences: Int = 1,
	val maxOccurrences: Int = Int.MAX_VALUE,
	val ignoreCase: Boolean = true,
	val mode: StrOccMode = StrOccMode.EQUALS,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
) {
	
	enum class StrOccMode {
		EQUALS,
		CONTAINS,
		STARTS_WITH,
		ENDS_WITH
	}
}