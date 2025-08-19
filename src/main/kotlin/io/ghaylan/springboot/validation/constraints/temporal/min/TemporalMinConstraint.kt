package io.ghaylan.springboot.validation.constraints.temporal.min

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import kotlin.reflect.KClass

data class TemporalMinConstraint(
    val value : String,
    val inclusive : Boolean,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()