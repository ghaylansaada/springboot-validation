package io.ghaylan.springboot.validation.exceptions

import io.ghaylan.springboot.validation.constraints.annotations.Required.RequirementCondition
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredConstraint
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExceptionsTest {

    @Test
    fun `ConstraintViolationException has correct message`() {
        val errors = listOf(
            ApiError(path = "email", code = ApiErrorCode.REQUIRED_VIOLATION),
            ApiError(path = "name", code = ApiErrorCode.REQUIRED_VIOLATION)
        )
        val exception = ConstraintViolationException(errors)
        assertEquals("Validation failed for 2 field(s).", exception.message)
        assertEquals(2, exception.errors.size)
    }

    @Test
    fun `ConstraintViolationException with single error`() {
        val errors = listOf(ApiError(path = "email", code = ApiErrorCode.EMAIL_FORMAT_VIOLATION))
        val exception = ConstraintViolationException(errors)
        assertEquals("Validation failed for 1 field(s).", exception.message)
    }

    @Test
    fun `ConstraintViolationException is a RuntimeException`() {
        val exception = ConstraintViolationException(emptyList())
        assertTrue(exception is RuntimeException)
    }

    @Test
    fun `InvalidConstraintDefinitionException has correct message`() {
        val constraint = RequiredConstraint(
            dependentField = "", condition = RequirementCondition.ALWAYS,
            groups = setOf(OnDefault::class), message = ""
        )
        val exception = InvalidConstraintDefinitionException(
            message = "min must be less than max",
            constraint = constraint
        )
        assertTrue(exception.message?.contains("min must be less than max") == true)
        assertTrue(exception.message?.contains("RequiredConstraint") == true)
        assertSame(constraint, exception.constraint)
    }

    @Test
    fun `InvalidConstraintDefinitionException with cause`() {
        val constraint = RequiredConstraint(
            dependentField = "", condition = RequirementCondition.ALWAYS,
            groups = setOf(OnDefault::class), message = ""
        )
        val cause = IllegalArgumentException("bad value")
        val exception = InvalidConstraintDefinitionException(
            message = "invalid config",
            constraint = constraint,
            cause = cause
        )
        assertSame(cause, exception.cause)
    }
}
