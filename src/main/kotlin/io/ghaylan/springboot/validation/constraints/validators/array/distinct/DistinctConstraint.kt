package io.ghaylan.springboot.validation.constraints.validators.array.distinct

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import io.ghaylan.springboot.validation.constraints.annotations.Distinct.DistinctMode
import kotlin.reflect.KClass

data class DistinctConstraint(
    val by : Set<String>,
    val mode : DistinctMode,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()