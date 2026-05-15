package io.ghaylan.springboot.validation.constraints.validators.comparison.valuein

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@ValueIn][io.ghaylan.springboot.validation.constraints.annotations.ValueIn]. */
data class ValueInConstraint(
	val values: Set<String>,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()