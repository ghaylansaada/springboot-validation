package io.ghaylan.springboot.validation.constraints.validators.string.nbr

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@NBR][io.ghaylan.springboot.validation.constraints.annotations.NBR]. */
data class NBRConstraint(
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()