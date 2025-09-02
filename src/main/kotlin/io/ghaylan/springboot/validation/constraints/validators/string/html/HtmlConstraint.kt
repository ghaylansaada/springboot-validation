package io.ghaylan.springboot.validation.constraints.validators.string.html

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class HtmlConstraint(
    val allowedTags : Set<String>,
    val allowedAttrs : Set<String>,
    val allowedProtocols : Set<String>,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()
