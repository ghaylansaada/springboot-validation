package io.ghaylan.springboot.validation.constraints.validators.string.country

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@ISOCountryCode][io.ghaylan.springboot.validation.constraints.annotations.ISOCountryCode]. */
data class ISOCountryConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
