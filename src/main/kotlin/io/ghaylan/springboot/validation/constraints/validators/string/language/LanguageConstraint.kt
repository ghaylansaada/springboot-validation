package io.ghaylan.springboot.validation.constraints.validators.string.language

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@LanguageCode][io.ghaylan.springboot.validation.constraints.annotations.LanguageCode]. */
data class LanguageConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
