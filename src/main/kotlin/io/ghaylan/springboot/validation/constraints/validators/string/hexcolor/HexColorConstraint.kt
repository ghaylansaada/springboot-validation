package io.ghaylan.springboot.validation.constraints.validators.string.hexcolor

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@HexColor][io.ghaylan.springboot.validation.constraints.annotations.HexColor]. */
data class HexColorConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()