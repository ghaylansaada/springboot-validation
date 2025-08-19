package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.constraints.string.email.EmailConstraint
import io.ghaylan.springboot.validation.constraints.string.email.EmailValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class EmailValidatorTest {

    private val validator = EmailValidator()
    private val constraint = EmailConstraint(
        groups = setOf(Any::class),
        messages = mapOf(
            "en" to "Invalid email address format",
            "fr" to "Format d'adresse e-mail invalide"
        )
    )
    
    private val context = ValidationContext(
        fieldName = "email",
        fieldPath = "user.email",
        type = null,
        location = ApiError.ErrorLocation.BODY,
        locale = Locale.ENGLISH,
        language = "en-US",
        stopOnFirstError = true,
        groups = setOf(Any::class),
        array = null,
        containerObject = null
    )

    @Test
    fun `validate should return null when value is null`() {
        // Given
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "test@example.com",
        "test.name@example.com",
        "test+name@example.com",
        "test@subdomain.example.com",
        "123@example.com",
        "test@example.co.uk"
    ])
    fun `validate should return null for valid email addresses`(email: String) {
        // When
        val result = validator.validate(email, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "test",
        "test@",
        "@example.com",
        "test@.com",
        "test@example.",
        "test@exam ple.com",
        "te st@example.com",
        "test@exam\nple.com"
    ])
    fun `validate should return ApiError for invalid email addresses`(email: String) {
        // When
        val result = validator.validate(email, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals(ApiError.ErrorLocation.BODY, result?.location)
        assertEquals("user.email", result?.field)
        assertEquals("EMAIL_INVALID", result?.code)
        assertEquals("Invalid email address format", result?.message)
    }
    
    @Test
    fun `validate should use localized message when available`() {
        // Given
        val frenchContext = context.copy(locale = Locale.FRENCH, language = "fr")
        
        // When
        val result = validator.validate("invalid", constraint, frenchContext)
        
        // Then
        assertNotNull(result)
        assertEquals("Format d'adresse e-mail invalide", result?.message)
    }
}

