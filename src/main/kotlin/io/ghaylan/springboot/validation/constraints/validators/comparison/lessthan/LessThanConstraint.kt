package io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@LessThan][io.ghaylan.springboot.validation.constraints.annotations.LessThan]. */
data class LessThanConstraint(
	val property: String,
	val inclusive: Boolean,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()