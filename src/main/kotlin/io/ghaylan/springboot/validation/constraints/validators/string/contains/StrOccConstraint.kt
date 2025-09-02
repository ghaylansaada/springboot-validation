package io.ghaylan.springboot.validation.constraints.validators.string.contains

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import io.ghaylan.springboot.validation.constraints.annotations.StrOcc.StrOccMode
import kotlin.reflect.KClass

data class StrOccConstraint(
    val value : String,
    val minOccurrences: Int,
    val maxOccurrences: Int,
    val ignoreCase : Boolean,
    val mode : StrOccMode,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()