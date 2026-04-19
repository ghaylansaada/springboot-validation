package io.ghaylan.springboot.validation.constraints.validators.required

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.annotations.Required.RequirementCondition
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class RequiredConstraint(
	val dependentField: String,
	val condition: RequirementCondition,
	override val groups: Set<KClass<*>>,
	override val messages: Set<MessageMetadata>
): ConstraintMetadata()