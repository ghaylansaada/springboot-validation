package io.ghaylan.springboot.validation.constraints.validators.string.uuid

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Uuid][io.ghaylan.springboot.validation.constraints.annotations.Uuid]. */
data class UuidConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
