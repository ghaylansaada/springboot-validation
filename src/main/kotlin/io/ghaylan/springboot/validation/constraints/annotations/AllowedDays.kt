package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays.AllowedDaysConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays.AllowedDaysValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import java.time.DayOfWeek
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value falls on one of the specified days of the week.
 * Skipped for time-only types (`LocalTime`, `OffsetTime`) that have no day-of-week component.
 *
 * @property days The allowed days of the week.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = AllowedDaysConstraint::class, validatedBy = [AllowedDaysValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class AllowedDays(
	val days: Array<DayOfWeek>,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)