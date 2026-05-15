package io.ghaylan.springboot.validation.model

import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.model.errors.ApiErrorCollector
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiErrorCollectorTest {
	
	@Test
	fun `throwIfNotEmpty does nothing when no errors`() {
		val collector = ApiErrorCollector()
		assertDoesNotThrow { collector.throwIfNotEmpty() }
	}
	
	@Test
	fun `throwIfNotEmpty throws when errors exist`() {
		val collector = ApiErrorCollector()
		collector.body(ApiErrorCode.REQUIRED_VIOLATION)
			.field("email")
			.message("Required")
		assertThrows(ConstraintViolationException::class.java) {
			collector.throwIfNotEmpty()
		}
	}
	
	@Test
	fun `collect returns empty list when no errors`() {
		val collector = ApiErrorCollector()
		assertTrue(collector.collect()
			.isEmpty())
	}
	
	@Test
	fun `collect returns error with message`() {
		val collector = ApiErrorCollector()
		collector.body(ApiErrorCode.REQUIRED_VIOLATION)
			.message("Required")
		val errors = collector.collect()
		assertEquals(1, errors.size)
		assertEquals("Required", errors[0].message)
	}
	
	@Test
	fun `body creates error with BODY location`() {
		val collector = ApiErrorCollector()
		collector.body(ApiErrorCode.REQUIRED_VIOLATION)
			.message("test")
		val errors = collector.collect()
		assertEquals(ApiError.ErrorLocation.BODY, errors[0].location)
	}
	
	@Test
	fun `query creates error with QUERY location`() {
		val collector = ApiErrorCollector()
		collector.query(ApiErrorCode.REQUIRED_VIOLATION)
			.message("test")
		val errors = collector.collect()
		assertEquals(ApiError.ErrorLocation.QUERY, errors[0].location)
	}
	
	@Test
	fun `header creates error with HEADER location`() {
		val collector = ApiErrorCollector()
		collector.header(ApiErrorCode.REQUIRED_VIOLATION)
			.message("test")
		val errors = collector.collect()
		assertEquals(ApiError.ErrorLocation.HEADER, errors[0].location)
	}
	
	@Test
	fun `path creates error with PATH location`() {
		val collector = ApiErrorCollector()
		collector.path(ApiErrorCode.REQUIRED_VIOLATION)
			.message("test")
		val errors = collector.collect()
		assertEquals(ApiError.ErrorLocation.PATH, errors[0].location)
	}
	
	@Test
	fun `business creates error with BUSINESS location`() {
		val collector = ApiErrorCollector()
		collector.business(ApiErrorCode.REQUIRED_VIOLATION)
			.message("test")
		val errors = collector.collect()
		assertEquals(ApiError.ErrorLocation.BUSINESS, errors[0].location)
	}
	
	@Test
	fun `multiple errors are collected`() {
		val collector = ApiErrorCollector()
		collector.body(ApiErrorCode.REQUIRED_VIOLATION)
			.field("email")
			.message("Email required")
		collector.body(ApiErrorCode.EMAIL_FORMAT_VIOLATION)
			.field("email")
			.message("Invalid email")
		val errors = collector.collect()
		assertEquals(2, errors.size)
	}
	
	@Test
	fun `field is set correctly`() {
		val collector = ApiErrorCollector()
		collector.body(ApiErrorCode.REQUIRED_VIOLATION)
			.field("user.email")
		val errors = collector.collect()
		assertEquals("user.email", errors[0].path)
	}
	
	@Test
	fun `data is set correctly`() {
		val collector = ApiErrorCollector()
		collector.body(ApiErrorCode.REQUIRED_VIOLATION)
			.data("key", "value")
		val errors = collector.collect()
		
		@Suppress("UNCHECKED_CAST")
		val data = errors[0].data as Map<String, Any?>
		assertEquals("value", data["key"])
	}
}
