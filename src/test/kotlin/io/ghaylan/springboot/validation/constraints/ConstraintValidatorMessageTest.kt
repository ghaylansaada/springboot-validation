package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.annotations.Required.RequirementCondition
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredConstraint
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ConstraintValidatorMessageTest {

    private fun requiredConstraint(message: String = "") = RequiredConstraint(
        dependentField = "", condition = RequirementCondition.ALWAYS,
        groups = setOf(OnDefault::class), message = message
    )

    @Test fun `uses default error message when no override`() = runTest {
        val ctx = TestHelper.defaultContext()
        val error = RequiredValidator.runValidation(null, requiredConstraint(), ctx)
        assertEquals("Required", error?.message)
    }

    @Test fun `error includes path from context`() = runTest {
        val ctx = TestHelper.defaultContext(fieldPath = "user.email")
        val error = RequiredValidator.runValidation(null, requiredConstraint(), ctx)
        assertEquals("user.email", error?.path)
    }

    @Test fun `error includes location from context`() = runTest {
        val ctx = TestHelper.defaultContext(
            location = io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation.QUERY
        )
        val error = RequiredValidator.runValidation(null, requiredConstraint(), ctx)
        assertEquals(io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation.QUERY, error?.location)
    }

    @Test fun `valid value returns null`() = runTest {
        val ctx = TestHelper.defaultContext()
        val error = RequiredValidator.runValidation("hello", requiredConstraint(), ctx)
        assertNull(error)
    }

    @Test fun `error code is REQUIRED_VIOLATION`() = runTest {
        val ctx = TestHelper.defaultContext()
        val error = RequiredValidator.runValidation(null, requiredConstraint(), ctx)
        assertEquals(ApiErrorCode.REQUIRED_VIOLATION, error?.code)
    }

    @Test fun `custom constraint message overrides validator default`() = runTest {
        val ctx = TestHelper.defaultContext()
        val error = RequiredValidator.runValidation(null, requiredConstraint(message = "My custom override"), ctx)
        assertEquals("My custom override", error?.message)
    }

    @Test fun `blank value fails same as null`() = runTest {
        val ctx = TestHelper.defaultContext()
        val error = RequiredValidator.runValidation("   ", requiredConstraint(), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.REQUIRED_VIOLATION, error?.code)
    }
}
