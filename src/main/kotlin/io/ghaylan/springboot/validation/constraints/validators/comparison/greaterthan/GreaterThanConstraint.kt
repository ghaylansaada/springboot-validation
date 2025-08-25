package io.ghaylan.springboot.validation.constraints.validators.comparison.greaterthan

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class GreaterThanConstraint(
    val property: String,
    val inclusive: Boolean,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()