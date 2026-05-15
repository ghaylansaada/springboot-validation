package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.nbr.NBRConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.nbr.NBRValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a valid National Business Register (NBR) identifier:
 * 2-8 characters, 7-digit prefix, and a weighted-sum modulo-23 checksum letter.
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = NBRConstraint::class, validatedBy = [NBRValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NBR(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)