package io.ghaylan.springboot.validation.constraints.temporal.alloweddays

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import java.time.DayOfWeek
import kotlin.reflect.KClass

data class AllowedDaysConstraint(
    val days : Set<DayOfWeek>,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()
