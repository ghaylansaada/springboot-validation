package io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import java.time.DayOfWeek
import kotlin.reflect.KClass

/** Constraint metadata for [@AllowedDays][io.ghaylan.springboot.validation.constraints.annotations.AllowedDays]. */
data class AllowedDaysConstraint(
	val days: Set<DayOfWeek>,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
