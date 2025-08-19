package io.ghaylan.springboot.validation.schema

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.constraints.required.RequiredConstraint
import io.ghaylan.springboot.validation.constraints.string.email.EmailConstraint
import io.ghaylan.springboot.validation.groups.CreateGroup
import io.ghaylan.springboot.validation.groups.DefaultGroup
import io.ghaylan.springboot.validation.integration.ValidateInput
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Method
import kotlin.reflect.KClass

@ExtendWith(MockitoExtension::class)
class ValidationSchemaBuilderTest {

    @Mock
    private lateinit var applicationContext: ApplicationContext
    
    @Mock
    private lateinit var requiredValidator: ConstraintValidator<String, RequiredConstraint>
    
    @Mock
    private lateinit var emailValidator: ConstraintValidator<String, EmailConstraint>
    
    private lateinit var validators: Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*, *>>>
    
    @BeforeEach
    fun setUp() {
        val requiredValidators = mapOf(
            TypeInfo(String::class, String::class) to requiredValidator
        )
        
        val emailValidators = mapOf(
            TypeInfo(String::class, String::class) to emailValidator
        )
        
        validators = mapOf(
            RequiredConstraint::class to requiredValidators,
            EmailConstraint::class to emailValidators
        )
    }
    
    @Test
    fun `generateSchemaForType should build schema for DTO class`() {
        // When
        val result = ValidationSchemaBuilder.generateSchemaForType(
            TestUser::class.java,
            validators
        )
        
        // Then
        assertNotNull(result)
        val (typeInfo, schema) = result!!
        assertEquals(TestUser::class, typeInfo.concreteType)
        assertTrue(schema.containsKey("email"))
        assertTrue(schema.containsKey("name"))
    }
    
    @Test
    fun `generateSchemaForType should handle nested objects`() {
        // When
        val result = ValidationSchemaBuilder.generateSchemaForType(
            TestUserWithAddress::class.java,
            validators
        )
        
        // Then
        assertNotNull(result)
        val (_, schema) = result!!
        assertTrue(schema.containsKey("address"))
        val addressSpec = schema["address"]!!
        assertFalse(addressSpec.nested.isEmpty())
        assertTrue(addressSpec.nested.containsKey("city"))
    }
    
    @Test
    fun `generateSchemaForType should handle collections`() {
        // When
        val result = ValidationSchemaBuilder.generateSchemaForType(
            TestUserWithRoles::class.java,
            validators
        )
        
        // Then
        assertNotNull(result)
        val (_, schema) = result!!
        assertTrue(schema.containsKey("roles"))
        val rolesSpec = schema["roles"]!!
        assertTrue(rolesSpec.typeInfo.isArray)
    }
    
    // Test classes
    data class TestUser(
        val email: String,
        val name: String
    )
    
    data class TestAddress(
        val street: String,
        val city: String,
        val zipCode: String
    )
    
    data class TestUserWithAddress(
        val email: String,
        val name: String,
        val address: TestAddress
    )
    
    data class TestUserWithRoles(
        val email: String,
        val roles: List<String>
    )
    
    // Test controller class with validation
    @Suppress("unused")
    class TestController {
        @ValidateInput(groups = [CreateGroup::class])
        fun createUser(
            @RequestBody user: TestUser,
            @RequestParam page: Int,
            @PathVariable id: Long
        ): TestUser {
            return user
        }
    }
}

