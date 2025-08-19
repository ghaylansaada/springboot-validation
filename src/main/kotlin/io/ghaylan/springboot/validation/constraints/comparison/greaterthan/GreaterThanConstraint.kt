package io.ghaylan.springboot.validation.constraints.comparison.greaterthan

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import kotlin.reflect.KClass

data class GreaterThanConstraint(
    val property: String,
    val inclusive: Boolean,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()