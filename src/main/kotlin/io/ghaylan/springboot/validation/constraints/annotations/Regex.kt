package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.regex.RegexConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.regex.RegexValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value (or each element in a collection) matches [pattern].
 *
 * @property pattern The regular expression the value must fully match.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = RegexConstraint::class, validatedBy = [RegexValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Regex(
	val pattern: String,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)