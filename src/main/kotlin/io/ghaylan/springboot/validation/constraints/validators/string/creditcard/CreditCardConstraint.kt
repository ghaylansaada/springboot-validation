package io.ghaylan.springboot.validation.constraints.validators.string.creditcard

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@CreditCard][io.ghaylan.springboot.validation.constraints.annotations.CreditCard]. */
data class CreditCardConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()