package io.ghaylan.springboot.validation.constraints.validators.temporal.future

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


object FutureValidator : ConstraintValidator<Temporal, FutureConstraint>()
{

    override suspend fun validate(
        value: Temporal?,
        constraint: FutureConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val now = value.now()

        if (value.isBefore(now)) {
            return ApiError(code = ApiErrorCode.FUTURE_VIOLATION, message = "Must be strictly in the future")
        }

        var maxTemporal = now
        val withinErrors = mutableListOf<String>()

        if (constraint.withinYears > 0) {
            maxTemporal = maxTemporal.plus(Period.ofYears(constraint.withinYears.toInt()))
            withinErrors.add("${constraint.withinYears} years")
        }

        if (constraint.withinMonths > 0) {
            maxTemporal = maxTemporal.plus(Period.ofMonths(constraint.withinMonths.toInt()))
            withinErrors.add("${constraint.withinMonths} months")
        }

        if (constraint.withinWeeks > 0) {
            maxTemporal = maxTemporal.plus(Period.ofWeeks(constraint.withinWeeks.toInt()))
            withinErrors.add("${constraint.withinWeeks} weeks")
        }

        if (constraint.withinDays > 0) {
            maxTemporal = maxTemporal.plus(Duration.ofDays(constraint.withinDays))
            withinErrors.add("${constraint.withinDays} days")
        }

        if (constraint.withinHours > 0) {
            maxTemporal = maxTemporal.plus(Duration.ofHours(constraint.withinHours))
            withinErrors.add("${constraint.withinHours} hours")
        }

        if (constraint.withinMinutes > 0) {
            maxTemporal = maxTemporal.plus(Duration.ofMinutes(constraint.withinMinutes))
            withinErrors.add("${constraint.withinMinutes} minutes")
        }

        if (constraint.withinSeconds > 0) {
            maxTemporal = maxTemporal.plus(Duration.ofSeconds(constraint.withinSeconds))
            withinErrors.add("${constraint.withinSeconds} seconds")
        }

        if (withinErrors.isNotEmpty() && value.isAfter(maxTemporal)) {
            return ApiError(code = ApiErrorCode.FUTURE_VIOLATION, message = "Must be in the future, within the next ${withinErrors.joinToString(", ")}.")
        }

        return null
    }
}