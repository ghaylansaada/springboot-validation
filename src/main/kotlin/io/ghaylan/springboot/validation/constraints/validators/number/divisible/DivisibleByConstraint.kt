package io.ghaylan.springboot.validation.constraints.validators.number.divisible

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@DivisibleBy][io.ghaylan.springboot.validation.constraints.annotations.DivisibleBy]. */
data class DivisibleByConstraint(
	val divisor: Double,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
