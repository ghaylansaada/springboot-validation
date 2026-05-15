package io.ghaylan.springboot.validation.constraints.validators.number.latitude

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Latitude][io.ghaylan.springboot.validation.constraints.annotations.Latitude]. */
data class LatitudeConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()