package io.ghaylan.springboot.validation.validators.required

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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RequiredValidatorTest {

    private val ctx = TestHelper.defaultContext()

    private fun constraint(
        dependentField: String = "",
        condition: RequirementCondition = RequirementCondition.ALWAYS
    ) = RequiredConstraint(
        dependentField = dependentField, condition = condition,
        groups = setOf(OnDefault::class), message = ""
    )

    @Test fun `non-null value is valid`() = runTest {
        assertNull(RequiredValidator.runValidation("hello", constraint(), ctx))
    }

    @Test fun `non-empty list is valid`() = runTest {
        assertNull(RequiredValidator.runValidation(listOf("a"), constraint(), ctx))
    }

    @Test fun `non-zero number is valid`() = runTest {
        assertNull(RequiredValidator.runValidation(42, constraint(), ctx))
    }

    @Test fun `null fails with ALWAYS condition`() = runTest {
        val error = RequiredValidator.runValidation(null, constraint(), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.REQUIRED_VIOLATION, error?.code)
    }

    @Test fun `empty string fails`() = runTest {
        val error = RequiredValidator.runValidation("", constraint(), ctx)
        assertNotNull(error)
        assertEquals(ApiErrorCode.REQUIRED_VIOLATION, error?.code)
    }

    @Test fun `blank string fails`() = runTest {
        val error = RequiredValidator.runValidation("   ", constraint(), ctx)
        assertNotNull(error)
    }

    @Test fun `empty list fails`() = runTest {
        val error = RequiredValidator.runValidation(emptyList<String>(), constraint(), ctx)
        assertNotNull(error)
    }

    @Test fun `empty map fails`() = runTest {
        val error = RequiredValidator.runValidation(emptyMap<String, String>(), constraint(), ctx)
        assertNotNull(error)
    }

    data class DependencyDto(val fieldA: String? = null, val fieldB: String? = null)

    @Test fun `IF_DEPENDENT_NULL triggers when dependent is null`() = runTest {
        val dto = DependencyDto(fieldA = null, fieldB = null)
        val ctx = TestHelper.contextWithSiblingProperty(dto, DependencyDto::class.java)
        val error = RequiredValidator.runValidation(null, constraint("fieldA", RequirementCondition.IF_DEPENDENT_NULL), ctx)
        assertNotNull(error)
        assertTrue(error?.message?.contains("missing") == true)
    }

    @Test fun `IF_DEPENDENT_NULL does not trigger when dependent has value`() = runTest {
        val dto = DependencyDto(fieldA = "present", fieldB = null)
        val ctx = TestHelper.contextWithSiblingProperty(dto, DependencyDto::class.java)
        assertNull(RequiredValidator.runValidation(null, constraint("fieldA", RequirementCondition.IF_DEPENDENT_NULL), ctx))
    }

    @Test fun `IF_DEPENDENT_NOT_NULL triggers when dependent has value`() = runTest {
        val dto = DependencyDto(fieldA = "present", fieldB = null)
        val ctx = TestHelper.contextWithSiblingProperty(dto, DependencyDto::class.java)
        val error = RequiredValidator.runValidation(null, constraint("fieldA", RequirementCondition.IF_DEPENDENT_NOT_NULL), ctx)
        assertNotNull(error)
        assertTrue(error?.message?.contains("provided") == true)
    }

    @Test fun `IF_DEPENDENT_NOT_NULL does not trigger when dependent is null`() = runTest {
        val dto = DependencyDto(fieldA = null, fieldB = null)
        val ctx = TestHelper.contextWithSiblingProperty(dto, DependencyDto::class.java)
        assertNull(RequiredValidator.runValidation(null, constraint("fieldA", RequirementCondition.IF_DEPENDENT_NOT_NULL), ctx))
    }
}
