package io.ghaylan.springboot.validation.constraints.validators.string.base64

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Base64][io.ghaylan.springboot.validation.constraints.annotations.Base64]. */
data class Base64Constraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
