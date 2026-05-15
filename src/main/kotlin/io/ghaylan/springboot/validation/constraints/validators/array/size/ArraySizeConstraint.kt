package io.ghaylan.springboot.validation.constraints.validators.array.size

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@ArraySize][io.ghaylan.springboot.validation.constraints.annotations.ArraySize]. */
data class ArraySizeConstraint(
	val min: Int,
	val max: Int,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()