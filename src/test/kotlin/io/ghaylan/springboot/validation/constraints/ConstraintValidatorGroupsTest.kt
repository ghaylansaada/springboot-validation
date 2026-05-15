package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.annotations.Required.RequirementCondition
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredConstraint
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredValidator
import io.ghaylan.springboot.validation.groups.OnCreate
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.groups.OnUpdate
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConstraintValidatorGroupsTest {

    private fun requiredConstraint(groups: Set<kotlin.reflect.KClass<*>>) = RequiredConstraint(
        dependentField = "", condition = RequirementCondition.ALWAYS,
        groups = groups, message = ""
    )

    @Nested
    inner class GroupFilteringTest {

        @Test fun `constraint with matching group is validated`() = runTest {
            val constraint = requiredConstraint(setOf(OnDefault::class))
            val ctx = TestHelper.defaultContext(groups = setOf(OnDefault::class))
            val error = RequiredValidator.runValidation(null, constraint, ctx)
            assertNotNull(error)
        }

        @Test fun `constraint with non-matching group is skipped`() = runTest {
            val constraint = requiredConstraint(setOf(OnCreate::class))
            val ctx = TestHelper.defaultContext(groups = setOf(OnUpdate::class))
            val error = RequiredValidator.runValidation(null, constraint, ctx)
            assertNull(error)
        }

        @Test fun `constraint with empty groups is always validated`() = runTest {
            val constraint = requiredConstraint(emptySet())
            val ctx = TestHelper.defaultContext(groups = setOf(OnDefault::class))
            val error = RequiredValidator.runValidation(null, constraint, ctx)
            assertNotNull(error)
        }

        @Test fun `context with empty groups skips group-restricted constraint`() = runTest {
            val constraint = requiredConstraint(setOf(OnCreate::class))
            val ctx = TestHelper.defaultContext(groups = emptySet())
            val error = RequiredValidator.runValidation(null, constraint, ctx)
            assertNull(error)
        }

        @Test fun `constraint with multiple groups matches if any overlap`() = runTest {
            val constraint = requiredConstraint(setOf(OnCreate::class, OnUpdate::class))
            val ctx = TestHelper.defaultContext(groups = setOf(OnUpdate::class))
            val error = RequiredValidator.runValidation(null, constraint, ctx)
            assertNotNull(error)
        }

        @Test fun `constraint with OnCreate matched against OnCreate context`() = runTest {
            val constraint = requiredConstraint(setOf(OnCreate::class))
            val ctx = TestHelper.defaultContext(groups = setOf(OnCreate::class))
            val error = RequiredValidator.runValidation(null, constraint, ctx)
            assertNotNull(error)
        }
    }
}
