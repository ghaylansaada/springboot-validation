package io.ghaylan.springboot.validation.constraints.validators.number.min

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@NumberMin][io.ghaylan.springboot.validation.constraints.annotations.NumberMin]. */
data class NumberMinConstraint(
	val value: Double,
	val inclusive: Boolean,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()