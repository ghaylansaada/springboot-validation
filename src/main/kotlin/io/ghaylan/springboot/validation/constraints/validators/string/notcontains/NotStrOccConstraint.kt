package io.ghaylan.springboot.validation.constraints.validators.string.notcontains

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.annotations.StrOcc.StrOccMode
import kotlin.reflect.KClass

/** Constraint metadata for [@NotStrOcc][io.ghaylan.springboot.validation.constraints.annotations.NotStrOcc]. */
data class NotStrOccConstraint(
	val value: String,
	val ignoreCase: Boolean,
	val mode: StrOccMode,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()