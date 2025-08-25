package io.ghaylan.springboot.validation.constraints.validators.string.nbr

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class NBRConstraint(
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()