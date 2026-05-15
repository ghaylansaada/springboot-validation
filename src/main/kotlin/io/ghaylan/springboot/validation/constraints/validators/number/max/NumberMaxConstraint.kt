package io.ghaylan.springboot.validation.constraints.validators.number.max

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@NumberMax][io.ghaylan.springboot.validation.constraints.annotations.NumberMax]. */
data class NumberMaxConstraint(
	val value: Double,
	val inclusive: Boolean,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()