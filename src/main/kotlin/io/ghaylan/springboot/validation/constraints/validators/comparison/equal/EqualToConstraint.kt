package io.ghaylan.springboot.validation.constraints.validators.comparison.equal

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass


/** Constraint metadata for [@EqualTo][io.ghaylan.springboot.validation.constraints.annotations.EqualTo]. */
data class EqualToConstraint(
	val property: String,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()