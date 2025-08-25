package io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import java.time.DayOfWeek
import kotlin.reflect.KClass

data class AllowedDaysConstraint(
    val days : Set<DayOfWeek>,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()
