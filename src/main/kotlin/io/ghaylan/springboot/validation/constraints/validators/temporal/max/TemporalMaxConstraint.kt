package io.ghaylan.springboot.validation.constraints.validators.temporal.max

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@TemporalMax][io.ghaylan.springboot.validation.constraints.annotations.TemporalMax]. */
data class TemporalMaxConstraint(
	val value: String,
	val inclusive: Boolean,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()