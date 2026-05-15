package io.ghaylan.springboot.validation.constraints.validators.string.contains

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.annotations.StrOcc.StrOccMode
import kotlin.reflect.KClass

/** Constraint metadata for [@StrOcc][io.ghaylan.springboot.validation.constraints.annotations.StrOcc]. */
data class StrOccConstraint(
	val value: String,
	val minOccurrences: Int,
	val maxOccurrences: Int,
	val ignoreCase: Boolean,
	val mode: StrOccMode,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()