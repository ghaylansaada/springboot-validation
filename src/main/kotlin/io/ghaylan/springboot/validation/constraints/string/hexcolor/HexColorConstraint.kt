package io.ghaylan.springboot.validation.constraints.string.hexcolor

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import kotlin.reflect.KClass

data class HexColorConstraint(
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()