package io.ghaylan.springboot.validation.constraints.validators.string.enums

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Enum][io.ghaylan.springboot.validation.constraints.annotations.Enum]. */
data class EnumConstraint(
	val ignoreCase: Boolean,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
