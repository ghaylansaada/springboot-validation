package io.ghaylan.springboot.validation.constraints.validators.string.length

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@TextLength][io.ghaylan.springboot.validation.constraints.annotations.TextLength]. */
data class TextLengthConstraint(
	val min: Int,
	val max: Int,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()