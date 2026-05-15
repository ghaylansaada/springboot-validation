package io.ghaylan.springboot.validation.constraints.validators.required

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.annotations.Required.RequirementCondition
import kotlin.reflect.KClass

/** Constraint metadata for [@Required][io.ghaylan.springboot.validation.constraints.annotations.Required]. */
data class RequiredConstraint(
	val dependentField: String,
	val condition: RequirementCondition,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()