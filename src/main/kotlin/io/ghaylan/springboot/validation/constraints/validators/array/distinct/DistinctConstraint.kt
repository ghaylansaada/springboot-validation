package io.ghaylan.springboot.validation.constraints.validators.array.distinct

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.annotations.Distinct.DistinctMode
import kotlin.reflect.KClass

/** Constraint metadata for [@Distinct][io.ghaylan.springboot.validation.constraints.annotations.Distinct]. */
data class DistinctConstraint(
	val by: Set<String>,
	val mode: DistinctMode,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()