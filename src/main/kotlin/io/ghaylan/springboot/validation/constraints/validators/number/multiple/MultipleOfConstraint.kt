package io.ghaylan.springboot.validation.constraints.validators.number.multiple

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@MultipleOf][io.ghaylan.springboot.validation.constraints.annotations.MultipleOf]. */
data class MultipleOfConstraint(
	val factor: Double,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
