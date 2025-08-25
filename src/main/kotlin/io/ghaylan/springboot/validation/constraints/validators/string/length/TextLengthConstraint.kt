package io.ghaylan.springboot.validation.constraints.validators.string.length

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class TextLengthConstraint(
    val min : Int,
    val max : Int,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()