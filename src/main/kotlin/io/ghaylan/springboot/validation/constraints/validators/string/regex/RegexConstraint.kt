package io.ghaylan.springboot.validation.constraints.validators.string.regex

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Regex][io.ghaylan.springboot.validation.constraints.annotations.Regex]. */
data class RegexConstraint(
	val pattern: String,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()