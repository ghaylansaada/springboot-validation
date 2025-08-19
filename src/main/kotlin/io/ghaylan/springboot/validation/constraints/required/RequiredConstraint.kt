package io.ghaylan.springboot.validation.constraints.required

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import io.ghaylan.springboot.validation.constraints.required.Required.RequirementCondition
import kotlin.reflect.KClass

data class RequiredConstraint(
    val dependentField : String,
    val condition : RequirementCondition,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()