package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.base64.Base64Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.base64.Base64Validator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence is a properly padded, decodable Base64 string.
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = Base64Constraint::class, validatedBy = [Base64Validator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Base64(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)