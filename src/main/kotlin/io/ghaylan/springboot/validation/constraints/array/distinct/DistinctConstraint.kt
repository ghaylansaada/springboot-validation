package io.ghaylan.springboot.validation.constraints.array.distinct

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import io.ghaylan.springboot.validation.constraints.array.distinct.Distinct.DistinctMode
import kotlin.reflect.KClass

data class DistinctConstraint(
    val by : Set<String>,
    val mode : DistinctMode,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()