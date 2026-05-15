package io.ghaylan.springboot.validation.constraints.validators.comparison.notequal

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@NotEqualTo][io.ghaylan.springboot.validation.constraints.annotations.NotEqualTo]. */
data class NotEqualToConstraint(
	val property: String,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()