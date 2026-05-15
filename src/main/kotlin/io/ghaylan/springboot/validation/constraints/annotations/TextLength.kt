package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.length.TextLengthConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.length.TextLengthValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated string's character length is within `[min, max]` (inclusive).
 *
 * @property min Minimum length (inclusive).
 * @property max Maximum length (inclusive).
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = TextLengthConstraint::class, validatedBy = [TextLengthValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class TextLength(
	val min: Int,
	val max: Int,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)