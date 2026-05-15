package io.ghaylan.springboot.validation.constraints.validators.map

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@MapSize][io.ghaylan.springboot.validation.constraints.annotations.MapSize]. */
data class MapSizeConstraint(
	val min: Int,
	val max: Int,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()