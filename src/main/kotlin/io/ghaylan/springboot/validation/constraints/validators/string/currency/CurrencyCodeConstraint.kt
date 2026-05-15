package io.ghaylan.springboot.validation.constraints.validators.string.currency

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@CurrencyCode][io.ghaylan.springboot.validation.constraints.annotations.CurrencyCode]. */
data class CurrencyCodeConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
