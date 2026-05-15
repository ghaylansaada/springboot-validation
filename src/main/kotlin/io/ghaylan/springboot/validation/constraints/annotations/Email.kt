package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.email.EmailConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.email.EmailValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a syntactically valid email address
 * (local part, single `@`, domain, and 1-6 character TLD).
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = EmailConstraint::class, validatedBy = [EmailValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Email(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)