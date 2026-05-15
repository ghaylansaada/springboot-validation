package io.ghaylan.springboot.validation.constraints.validators.comparison.greaterthan

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@GreaterThan][io.ghaylan.springboot.validation.constraints.annotations.GreaterThan]. */
data class GreaterThanConstraint(
	val property: String,
	val inclusive: Boolean,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()