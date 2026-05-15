package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.notcontains.NotStrOccConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.notcontains.NotStrOccValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated string does NOT match [value] according to [mode]
 * (must not equal, contain, start with, or end with the substring). Repeatable.
 *
 * @property value The forbidden substring.
 * @property ignoreCase Whether the match is case-insensitive.
 * @property mode The matching strategy (EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH).
 * @property message Optional error message.
 */
@Repeatable
@MustBeDocumented
@Constraint(metadata = NotStrOccConstraint::class, validatedBy = [NotStrOccValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NotStrOcc(
	val value: String,
	val ignoreCase: Boolean = true,
	val mode: StrOcc.StrOccMode = StrOcc.StrOccMode.EQUALS,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)