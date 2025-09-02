package io.ghaylan.springboot.validation.constraints.validators.temporal.alloweddays

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.OffsetTime
import java.time.format.TextStyle
import java.time.temporal.Temporal

object AllowedDaysValidator : ConstraintValidator<Temporal, AllowedDaysConstraint>()
{

    override suspend fun validate(
        value: Temporal?,
        constraint: AllowedDaysConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        if (value is LocalTime || value is OffsetTime) return null

        val dayOfWeek = runCatching {
            DayOfWeek.from(value)
        }.getOrNull()

        if (constraint.days.contains(dayOfWeek)) return null

        val displayedDays = constraint.days.joinToString(", ", transform = { it.getDisplayName(TextStyle.FULL, context.locale) })

        return ApiError(code = ApiErrorCode.DAY_OF_WEEK_VIOLATION, message = "Must fall on one of the following days: $displayedDays")
    }
}