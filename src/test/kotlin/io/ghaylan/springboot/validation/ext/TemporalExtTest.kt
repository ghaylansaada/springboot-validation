package io.ghaylan.springboot.validation.ext

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TemporalExtTest {

    @Test
    fun `isBefore with LocalDate`() {
        val date1 = LocalDate.of(2024, 1, 1)
        val date2 = LocalDate.of(2024, 6, 1)
        assertTrue(date1.isBefore(date2))
        assertFalse(date2.isBefore(date1))
    }

    @Test
    fun `isAfter with LocalDate`() {
        val date1 = LocalDate.of(2024, 6, 1)
        val date2 = LocalDate.of(2024, 1, 1)
        assertTrue(date1.isAfter(date2))
        assertFalse(date2.isAfter(date1))
    }

    @Test
    fun `isEqual with LocalDate`() {
        val date = LocalDate.of(2024, 1, 1)
        assertTrue(date.isEqual(LocalDate.of(2024, 1, 1)))
        assertFalse(date.isEqual(LocalDate.of(2024, 1, 2)))
    }

    @Test
    fun `isBeforeOrEqual with LocalDate`() {
        val date1 = LocalDate.of(2024, 1, 1)
        val date2 = LocalDate.of(2024, 1, 1)
        val date3 = LocalDate.of(2024, 6, 1)
        assertTrue(date1.isBeforeOrEqual(date2))
        assertTrue(date1.isBeforeOrEqual(date3))
        assertFalse(date3.isBeforeOrEqual(date1))
    }

    @Test
    fun `isAfterOrEqual with LocalDate`() {
        val date1 = LocalDate.of(2024, 6, 1)
        val date2 = LocalDate.of(2024, 6, 1)
        val date3 = LocalDate.of(2024, 1, 1)
        assertTrue(date1.isAfterOrEqual(date2))
        assertTrue(date1.isAfterOrEqual(date3))
        assertFalse(date3.isAfterOrEqual(date1))
    }

    @Test
    fun `isBefore with LocalDateTime`() {
        val dt1 = LocalDateTime.of(2024, 1, 1, 10, 0)
        val dt2 = LocalDateTime.of(2024, 1, 1, 12, 0)
        assertTrue(dt1.isBefore(dt2))
    }

    @Test
    fun `isAfter with Instant`() {
        val i1 = Instant.parse("2024-06-01T00:00:00Z")
        val i2 = Instant.parse("2024-01-01T00:00:00Z")
        assertTrue(i1.isAfter(i2))
    }

    @Test
    fun `toTemporal parses LocalDate`() {
        val result = "2024-01-15".toTemporal(LocalDate::class)
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    @Test
    fun `toTemporal parses LocalDateTime`() {
        val result = "2024-01-15T10:30:00".toTemporal(LocalDateTime::class)
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 0), result)
    }

    @Test
    fun `toTemporal parses Instant`() {
        val result = "2024-01-15T10:30:00Z".toTemporal(Instant::class)
        assertEquals(Instant.parse("2024-01-15T10:30:00Z"), result)
    }

    @Test
    fun `toTemporal parses LocalTime`() {
        val result = "10:30:00".toTemporal(LocalTime::class)
        assertEquals(LocalTime.of(10, 30, 0), result)
    }

    @Test
    fun `now returns correct type for LocalDate`() {
        val now = LocalDate.now()
        val result = now.now()
        assertTrue(result is LocalDate)
    }

    @Test
    fun `now returns correct type for LocalDateTime`() {
        val now = LocalDateTime.now()
        val result = now.now()
        assertTrue(result is LocalDateTime)
    }

    @Test
    fun `now returns correct type for Instant`() {
        val now = Instant.now()
        val result = now.now()
        assertTrue(result is Instant)
    }

    @Test
    fun `toTemporal throws for unsupported type`() {
        assertThrows(IllegalArgumentException::class.java) {
            "2024-01-01".toTemporal(java.time.Year::class)
        }
    }
}
