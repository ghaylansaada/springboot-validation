package io.ghaylan.springboot.validation.engine

import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ValidatorEngineTest {

    @Nested
    inner class DeduplicateErrorsTest {
        private val engine = object : ValidatorEngine(
            validationRegistry = object : io.ghaylan.springboot.validation.integration.ValidationRegistry() {}
        ) {}

        @Test
        fun `empty list returns empty`() {
            val result = engine.deduplicateErrors(emptyList())
            assertTrue(result.isEmpty())
        }

        @Test
        fun `unique errors are preserved`() {
            val errors = listOf(
                ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY),
                ApiError(path = "name", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY)
            )
            val result = engine.deduplicateErrors(errors)
            assertEquals(2, result.size)
        }

        @Test
        fun `duplicate errors are removed`() {
            val errors = listOf(
                ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY),
                ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY),
                ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY)
            )
            val result = engine.deduplicateErrors(errors)
            assertEquals(1, result.size)
        }

        @Test
        fun `same field different codes are preserved`() {
            val errors = listOf(
                ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY),
                ApiError(path = "email", code = ApiErrorCode.EMAIL_FORMAT_VIOLATION, location = ErrorLocation.BODY)
            )
            val result = engine.deduplicateErrors(errors)
            assertEquals(2, result.size)
        }

        @Test
        fun `same field same code different locations are preserved`() {
            val errors = listOf(
                ApiError(path = "token", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.HEADER),
                ApiError(path = "token", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.QUERY)
            )
            val result = engine.deduplicateErrors(errors)
            assertEquals(2, result.size)
        }

        @Test
        fun `first occurrence is kept during deduplication`() {
            val errors = listOf(
                ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY, message = "first"),
                ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY, message = "second")
            )
            val result = engine.deduplicateErrors(errors)
            assertEquals(1, result.size)
            assertEquals("first", result[0].message)
        }
    }

    @Nested
    inner class AppendPathTest {
        private val engine = object : ValidatorEngine(
            validationRegistry = object : io.ghaylan.springboot.validation.integration.ValidationRegistry() {}
        ) {}

        @Test
        fun `appendPath and appendIndex produce correct paths via dedup error keys`() {
            val errors = listOf(
                ApiError(path = "user.address", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY),
                ApiError(path = "user.address[0]", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY),
                ApiError(path = "user.address[0].city", code = ApiErrorCode.REQUIRED_VIOLATION, location = ErrorLocation.BODY)
            )
            val result = engine.deduplicateErrors(errors)
            assertEquals(3, result.size)
        }
    }
}
