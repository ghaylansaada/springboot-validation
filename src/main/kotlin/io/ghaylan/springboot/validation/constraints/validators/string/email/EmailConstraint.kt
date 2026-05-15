package io.ghaylan.springboot.validation.constraints.validators.string.email

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Email][io.ghaylan.springboot.validation.constraints.annotations.Email]. */
data class EmailConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()