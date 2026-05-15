package io.ghaylan.springboot.validation.constraints.validators.temporal.min

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@TemporalMin][io.ghaylan.springboot.validation.constraints.annotations.TemporalMin]. */
data class TemporalMinConstraint(
	val value: String,
	val inclusive: Boolean,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()