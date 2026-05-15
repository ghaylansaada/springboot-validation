package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.language.LanguageConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.language.LanguageValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a valid ISO language tag: `"xx"` (ISO 639-1) or `"xx-YY"` (with ISO 3166-1 region).
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = LanguageConstraint::class, validatedBy = [LanguageValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class LanguageCode(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)