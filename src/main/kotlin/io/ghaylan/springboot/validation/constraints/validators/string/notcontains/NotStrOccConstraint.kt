package io.ghaylan.springboot.validation.constraints.validators.string.notcontains

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import io.ghaylan.springboot.validation.constraints.annotations.StrOcc.StrOccMode
import kotlin.reflect.KClass

data class NotStrOccConstraint(
    val value : String,
    val ignoreCase : Boolean,
    val mode : StrOccMode,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()