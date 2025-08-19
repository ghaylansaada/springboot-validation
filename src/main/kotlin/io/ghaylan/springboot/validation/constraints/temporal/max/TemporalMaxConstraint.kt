package io.ghaylan.springboot.validation.constraints.temporal.max

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import kotlin.reflect.KClass

data class TemporalMaxConstraint(
    val value : String,
    val inclusive : Boolean,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()