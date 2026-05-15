package io.ghaylan.springboot.validation.model

import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorBuilder
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiErrorBuilderTest {
	
	@Test
	fun `build creates error with location and code`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).build()
		assertEquals(ApiError.ErrorLocation.BODY, error.location)
		assertEquals(ApiErrorCode.REQUIRED_VIOLATION, error.code)
	}
	
	@Test
	fun `build with field path`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).field("user.email")
			.build()
		assertEquals("user.email", error.path)
	}
	
	@Test
	fun `build with message`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).message("Email is required")
			.build()
		assertEquals("Email is required", error.message)
	}
	
	@Test
	fun `build with data object`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).data("some data")
			.build()
		assertEquals("some data", error.data)
	}
	
	@Test
	fun `build with data map`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).data("key1", "value1")
			.data("key2", 42)
			.build()
		val data = error.data as Map<*, *>
		assertEquals("value1", data["key1"])
		assertEquals(42, data["key2"])
	}
	
	@Test
	fun `data map takes precedence over data object`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).data("should be overridden")
			.data("key", "value")
			.build()
		assertTrue(error.data is Map<*, *>)
	}
	
	@Test
	fun `build with all locations`() {
		assertEquals(ApiError.ErrorLocation.QUERY, ApiErrorBuilder(ApiError.ErrorLocation.QUERY, ApiErrorCode.REQUIRED_VIOLATION).build().location)
		assertEquals(ApiError.ErrorLocation.HEADER, ApiErrorBuilder(ApiError.ErrorLocation.HEADER, ApiErrorCode.REQUIRED_VIOLATION).build().location)
		assertEquals(ApiError.ErrorLocation.PATH, ApiErrorBuilder(ApiError.ErrorLocation.PATH, ApiErrorCode.REQUIRED_VIOLATION).build().location)
		assertEquals(ApiError.ErrorLocation.BUSINESS,
			ApiErrorBuilder(ApiError.ErrorLocation.BUSINESS, ApiErrorCode.REQUIRED_VIOLATION).build().location)
	}
	
	@Test
	fun `build with empty data map returns null data`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).build()
		assertNull(error.data)
	}
	
	@Test
	fun `message is initially null`() {
		val error = ApiErrorBuilder(ApiError.ErrorLocation.BODY, ApiErrorCode.REQUIRED_VIOLATION).build()
		assertNull(error.message)
	}
}
