package io.ghaylan.springboot.validation.constraints.validators.temporal.past

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

data class PastConstraint(
    val withinSeconds : Long = 0,
    val withinMinutes : Long = 0,
    val withinHours : Long = 0,
    val withinDays : Long = 0,
    val withinWeeks : Long = 0,
    val withinMonths : Long = 0,
    val withinYears : Long = 0,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()