package io.ghaylan.springboot.validation.engine

import io.ghaylan.springboot.validation.accessor.AccessorFactory
import io.ghaylan.springboot.validation.accessor.AccessorRegistry
import io.ghaylan.springboot.validation.accessor.FieldAccessor
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.constraints.required.RequiredConstraint
import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.groups.DefaultGroup
import io.ghaylan.springboot.validation.integration.ValidationRegistry
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.schema.RequestInputSchema
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.reflect.KClass

@ExtendWith(MockitoExtension::class)
class ValidatorEngineTest {

    @Mock
    private lateinit var validationRegistry: ValidationRegistry
    
    @Mock
    private lateinit var mockValidator: ConstraintValidator<String, RequiredConstraint>
    
    private lateinit var validatorEngine: ValidatorEngine
    
    @BeforeEach
    fun setUp() {
        validatorEngine = ValidatorEngine(validationRegistry)
        AccessorRegistry.clear()
    }
    
    @Test
    fun `validateRequest should process request body validation`() {
        // Given
        val requestId = "test-request"
        val body = TestDto("test@example.com")
        val constraint = RequiredConstraint(groups = setOf(DefaultGroup::class))
        val propertySpec = createPropertySpec(
            "email", 
            String::class.java,
            mapOf(constraint to mockValidator)
        )
        val validationConfig = RequestInputSchema.ValidationConfig(
            validateBody = true,
            validateQuery = false,
            validateHeaders = false,
            validatePathVariables = false,
            singleErrorPerField = true,
            groups = setOf(DefaultGroup::class)
        )
        val schema = RequestInputSchema(
            id = requestId,
            requestBody = mapOf("email" to propertySpec),
            requestBodyTypeInfo = TypeInfo(TestDto::class, TestDto::class),
            validationConfig = validationConfig
        )
        
        `when`(validationRegistry.getSchemaByRequest(requestId)).thenReturn(schema)
        `when`(mockValidator.validate(any(), eq(constraint), any())).thenReturn(null)
        
        // When
        val errors = validatorEngine.validateRequest(
            id = requestId,
            body = body,
            params = emptyMap(),
            headers = emptyMap(),
            pathVariables = emptyMap(),
            locale = Locale.ENGLISH
        )
        
        // Then
        assertTrue(errors.isEmpty())
    }
    
    @Test
    fun `validateRequest should collect errors`() {
        // Given
        val requestId = "test-request"
        val body = TestDto(null)
        val constraint = RequiredConstraint(groups = setOf(DefaultGroup::class))
        val propertySpec = createPropertySpec(
            "email", 
            String::class.java,
            mapOf(constraint to mockValidator)
        )
        val validationConfig = RequestInputSchema.ValidationConfig(
            validateBody = true,
            validateQuery = false,
            validateHeaders = false,
            validatePathVariables = false,
            singleErrorPerField = true,
            groups = setOf(DefaultGroup::class)
        )
        val schema = RequestInputSchema(
            id = requestId,
            requestBody = mapOf("email" to propertySpec),
            requestBodyTypeInfo = TypeInfo(TestDto::class, TestDto::class),
            validationConfig = validationConfig
        )
        
        val apiError = ApiError(
            field = "email",
            code = "REQUIRED",
            location = ApiError.ErrorLocation.BODY,
            message = "Field is required",
            data = null,
            messages = null
        )
        
        `when`(validationRegistry.getSchemaByRequest(requestId)).thenReturn(schema)
        `when`(mockValidator.validate(any(), eq(constraint), any())).thenReturn(apiError)
        
        // When
        val errors = validatorEngine.validateRequest(
            id = requestId,
            body = body,
            params = emptyMap(),
            headers = emptyMap(),
            pathVariables = emptyMap(),
            locale = Locale.ENGLISH
        )
        
        // Then
        assertEquals(1, errors.size)
        assertEquals("email", errors[0].field)
        assertEquals("REQUIRED", errors[0].code)
        assertEquals(ApiError.ErrorLocation.BODY, errors[0].location)
    }
    
    @Test
    fun `validate should throw when validation fails`() {
        // Given
        val dto = TestDto(null)
        val constraint = RequiredConstraint(groups = setOf(DefaultGroup::class))
        val propertySpec = createPropertySpec(
            "email", 
            String::class.java,
            mapOf(constraint to mockValidator)
        )
        val schema = TypeInfo(TestDto::class, TestDto::class) to mapOf("email" to propertySpec)
        
        val apiError = ApiError(
            field = "email",
            code = "REQUIRED",
            location = ApiError.ErrorLocation.BODY,
            message = "Field is required",
            data = null,
            messages = null
        )
        
        `when`(validationRegistry.resolveSchemaByClass(TestDto::class.java)).thenReturn(schema)
        `when`(mockValidator.validate(any(), eq(constraint), any())).thenReturn(apiError)
        
        // When/Then
        val exception = assertThrows(ConstraintViolationException::class.java) {
            validatorEngine.validate(dto)
        }
        
        assertEquals(1, exception.errors.size)
        assertEquals("email", exception.errors[0].field)
        assertEquals("REQUIRED", exception.errors[0].code)
    }
    
    @Test
    fun `standardizeLanguage should format locale correctly`() {
        // Given
        val locale = Locale("en", "US")
        
        // When
        val result = validatorEngine.standardizeLanguage(locale)
        
        // Then
        assertEquals("en-US", result)
    }
    
    // Helper functions and classes
    private fun <T : Any> createPropertySpec(
        name: String,
        type: Class<T>,
        constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>
    ): RequestInputSchema.PropertySpec {
        val accessor = AccessorRegistry.getOrCreate(TestDto::class.java, name)
        val typeInfo = TypeInfo(type.kotlin, type.kotlin)
        
        return RequestInputSchema.PropertySpec(
            name = name,
            typeInfo = typeInfo,
            accessor = accessor,
            nested = emptyMap(),
            constraints = constraints
        )
    }
    
    private fun <T> any(): T {
        return null as T
    }
    
    private fun <T> eq(value: T): T {
        return value
    }
    
    data class TestDto(val email: String?)
}

