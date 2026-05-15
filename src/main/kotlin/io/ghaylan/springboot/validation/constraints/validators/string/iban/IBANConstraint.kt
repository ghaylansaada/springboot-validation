package io.ghaylan.springboot.validation.constraints.validators.string.iban

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@IBAN][io.ghaylan.springboot.validation.constraints.annotations.IBAN]. */
data class IBANConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()