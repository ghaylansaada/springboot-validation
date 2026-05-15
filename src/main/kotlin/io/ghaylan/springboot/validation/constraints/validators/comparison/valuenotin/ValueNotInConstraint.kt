package io.ghaylan.springboot.validation.constraints.validators.comparison.valuenotin

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@ValueNotIn][io.ghaylan.springboot.validation.constraints.annotations.ValueNotIn]. */
data class ValueNotInConstraint(
	val values: Set<String>,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()