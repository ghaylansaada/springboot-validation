package io.ghaylan.springboot.validation.constraints.validators.number.max

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class NumberMaxConstraint(
    val value : Double,
    val inclusive : Boolean,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()