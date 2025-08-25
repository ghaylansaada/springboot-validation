package io.ghaylan.springboot.validation.constraints.validators.temporal.past

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.ext.isAfter
import io.ghaylan.springboot.validation.ext.isBefore
import io.ghaylan.springboot.validation.ext.now
import io.ghaylan.springboot.validation.model.errors.ApiError
import java.time.Duration
import java.time.Period
import java.time.temporal.Temporal


object PastValidator : ConstraintValidator<Temporal, PastConstraint>()
{

    override suspend fun validate(
        value: Temporal?,
        constraint: PastConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val now = value.now()

        if (value.isAfter(now)) {
            return ApiError(code = ApiErrorCode.PAST_VIOLATION, message = "Must be strictly in the past")
        }

        var maxTemporal = now
        val withinErrors = mutableListOf<String>()

        if (constraint.withinYears > 0) {
            maxTemporal = maxTemporal.minus(Period.ofYears(constraint.withinYears.toInt()))
            withinErrors.add("${constraint.withinYears} years")
        }

        if (constraint.withinMonths > 0) {
            maxTemporal = maxTemporal.minus(Period.ofMonths(constraint.withinMonths.toInt()))
            withinErrors.add("${constraint.withinMonths} months")
        }

        if (constraint.withinWeeks > 0) {
            maxTemporal = maxTemporal.minus(Period.ofWeeks(constraint.withinWeeks.toInt()))
            withinErrors.add("${constraint.withinWeeks} weeks")
        }

        if (constraint.withinDays > 0) {
            maxTemporal = maxTemporal.minus(Duration.ofDays(constraint.withinDays))
            withinErrors.add("${constraint.withinDays} days")
        }

        if (constraint.withinHours > 0) {
            maxTemporal = maxTemporal.minus(Duration.ofHours(constraint.withinHours))
            withinErrors.add("${constraint.withinHours} hours")
        }

        if (constraint.withinMinutes > 0) {
            maxTemporal = maxTemporal.minus(Duration.ofMinutes(constraint.withinMinutes))
            withinErrors.add("${constraint.withinMinutes} minutes")
        }

        if (constraint.withinSeconds > 0) {
            maxTemporal = maxTemporal.minus(Duration.ofSeconds(constraint.withinSeconds))
            withinErrors.add("${constraint.withinSeconds} seconds")
        }

        if (withinErrors.isNotEmpty() && value.isBefore(maxTemporal)) {
            return ApiError(code = ApiErrorCode.PAST_VIOLATION, message = "Must be in the past, within the last ${withinErrors.joinToString(", ")}.")
        }

        return null
    }
}