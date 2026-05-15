package io.ghaylan.springboot.validation.constraints.validators.number.longitude

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Longitude][io.ghaylan.springboot.validation.constraints.annotations.Longitude]. */
data class LongitudeConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()