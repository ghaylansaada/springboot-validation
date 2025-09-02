package io.ghaylan.springboot.validation.ext

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.time.temporal.Temporal
import kotlin.reflect.KClass

/**
 * Compares this [Temporal] to another [Temporal] to check if it occurs before the other.
 *
 * @receiver The temporal instance to compare.
 * @param other The temporal instance to compare against.
 * @return `true` if this temporal is before [other], `false` otherwise.
 */
fun Temporal.isBefore(other: Temporal): Boolean = compareTemporal(this, other) < 0

/**
 * Compares this [Temporal] to another [Temporal] to check if it occurs after the other.
 *
 * @return `true` if this temporal is after [other], `false` otherwise.
 */
fun Temporal.isAfter(other: Temporal): Boolean = compareTemporal(this, other) > 0

/**
 * Compares this [Temporal] to another [Temporal] to check if they are equal.
 *
 * @return `true` if this temporal is equal to [other], `false` otherwise.
 */
fun Temporal.isEqual(other: Temporal): Boolean = compareTemporal(this, other) == 0

/**
 * Checks if this [Temporal] is before or equal to another [Temporal].
 *
 * @return `true` if this temporal is before or equal to [other], `false` otherwise.
 */
fun Temporal.isBeforeOrEqual(other: Temporal): Boolean = compareTemporal(this, other) <= 0

/**
 * Checks if this [Temporal] is after or equal to another [Temporal].
 *
 * @return `true` if this temporal is after or equal to [other], `false` otherwise.
 */
fun Temporal.isAfterOrEqual(other: Temporal): Boolean = compareTemporal(this, other) >= 0


/**
 * Returns the current instance of the same temporal type as this [Temporal].
 *
 * Example:
 * ```
 * LocalDate.now() == LocalDate().now()
 * ```
 *
 * @receiver The temporal type to get the current value for.
 * @return Current temporal of the same type.
 */
internal fun Temporal.now() : Temporal
{
    return when (this)
    {
        is LocalDate -> LocalDate.now()
        is LocalTime -> LocalTime.now()
        is OffsetTime -> OffsetTime.now()
        is LocalDateTime -> LocalDateTime.now()
        is ZonedDateTime -> ZonedDateTime.now()
        is OffsetDateTime -> OffsetDateTime.now()
        else -> Instant.now()
    }
}

/**
 * Converts a [String] into a [Temporal] instance of the specified [clazz].
 *
 * Supported types: LocalDate, LocalTime, OffsetTime, LocalDateTime, ZonedDateTime, OffsetDateTime, Instant.
 *
 * @receiver The string representation of the temporal.
 * @param clazz The target temporal class.
 * @return A temporal instance of type [clazz].
 * @throws IllegalArgumentException If the temporal type is unsupported.
 */
internal fun String.toTemporal(clazz : KClass<out Temporal>) : Temporal
{
    return when (clazz)
    {
        LocalDate::class -> LocalDate.parse(this)
        LocalTime::class -> LocalTime.parse(this)
        OffsetTime::class -> OffsetTime.parse(this)
        LocalDateTime::class -> LocalDateTime.parse(this)
        ZonedDateTime::class -> ZonedDateTime.parse(this)
        OffsetDateTime::class -> OffsetDateTime.parse(this)
        Instant::class -> Instant.parse(this)
        else -> throw IllegalArgumentException("Unsupported temporal type: ${clazz.java.name}")
    }
}

/**
 * Compares two [Temporal] instances of the same type.
 *
 * @param value1 First temporal instance.
 * @param value2 Second temporal instance.
 * @return Negative if value1 < value2, zero if equal, positive if value1 > value2.
 * @throws IllegalArgumentException If the temporal types are unsupported or mismatched.
 */
private fun compareTemporal(value1: Temporal, value2: Temporal): Int
{
    return when (value1)
    {
        is LocalDate if value2 is LocalDate -> value1.compareTo(value2)
        is LocalTime if value2 is LocalTime -> value1.compareTo(value2)
        is LocalDateTime if value2 is LocalDateTime -> value1.compareTo(value2)
        is ZonedDateTime if value2 is ZonedDateTime -> value1.compareTo(value2)
        is OffsetDateTime if value2 is OffsetDateTime -> value1.compareTo(value2)
        is OffsetTime if value2 is OffsetTime -> value1.compareTo(value2)
        is Instant if value2 is Instant -> value1.compareTo(value2)
        else -> throw IllegalArgumentException("Unsupported or mismatched Temporal types: ${value1::class.simpleName} vs ${value2::class.simpleName}")
    }
}