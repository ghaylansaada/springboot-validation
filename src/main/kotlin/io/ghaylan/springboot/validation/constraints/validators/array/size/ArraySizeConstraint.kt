package io.ghaylan.springboot.validation.constraints.validators.array.size

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class ArraySizeConstraint(
    val min : Int,
    val max : Int,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()