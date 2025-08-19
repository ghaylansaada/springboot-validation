package io.ghaylan.springboot.validation.constraints.string

import io.ghaylan.springboot.validation.constraints.string.url.UrlConstraint
import io.ghaylan.springboot.validation.constraints.string.url.UrlValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class UrlValidatorTest {

    private val validator = UrlValidator()
    private val context = ValidationContext(
        fieldName = "website",
        fieldPath = "user.website",
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
        val constraint = UrlConstraint(groups = setOf(Any::class))
        val value = null
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "https://www.example.com",
        "http://example.com",
        "https://example.com/path/to/resource",
        "http://example.com:8080/path",
        "https://example.com/path?query=value",
        "https://example.com#fragment",
        "http://localhost",
        "http://127.0.0.1",
        "http://127.0.0.1:8080",
        "http://user:password@example.com",
        "https://sub.domain.example.com"
    ])
    fun `validate should return null for valid URLs`(url: String) {
        // Given
        val constraint = UrlConstraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(url, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "not a url",
        "ftp://example.com",  // Only if protocol is restricted to http/https
        "://example.com",
        "http:/example.com",
        "http//example.com",
        "http://.com",
        "http://example",     // Missing TLD
        "htt://example.com",
        ".com",
        "example.com",        // Missing protocol
        "http:example.com",
        "http://:8080/",      // Missing hostname
        "http://example-.com" // Invalid hostname
    ])
    fun `validate should return ApiError for invalid URLs`(url: String) {
        // Given
        val constraint = UrlConstraint(groups = setOf(Any::class))
        
        // When
        val result = validator.validate(url, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("user.website", result?.field)
        assertEquals("URL_INVALID", result?.code)
        assertNotNull(result?.data)
        assertEquals(url, result?.data?.get("actual"))
    }
    
    @Test
    fun `validate should return null when requireTld is false and host has no TLD`() {
        // Given
        val constraint = UrlConstraint(requireTld = false, groups = setOf(Any::class))
        val url = "http://localhost"
        
        // When
        val result = validator.validate(url, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should return ApiError when requireTld is true and host has no TLD`() {
        // Given
        val constraint = UrlConstraint(requireTld = true, groups = setOf(Any::class))
        val url = "http://example"
        
        // When
        val result = validator.validate(url, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("URL_INVALID", result?.code)
    }
    
    @Test
    fun `validate should handle protocol restrictions`() {
        // Given
        val constraint = UrlConstraint(
            protocols = arrayOf("https"),
            groups = setOf(Any::class)
        )
        val httpUrl = "http://example.com"
        val httpsUrl = "https://example.com"
        
        // When
        val httpResult = validator.validate(httpUrl, constraint, context)
        val httpsResult = validator.validate(httpsUrl, constraint, context)
        
        // Then
        assertNotNull(httpResult)
        assertEquals("URL_INVALID_PROTOCOL", httpResult?.code)
        assertNull(httpsResult)
    }
    
    @Test
    fun `validate should handle port restrictions`() {
        // Given
        val constraint = UrlConstraint(
            ports = arrayOf(8080, 443),
            groups = setOf(Any::class)
        )
        val url1 = "http://example.com:8080"
        val url2 = "https://example.com:443"
        val url3 = "http://example.com:9090"
        
        // When
        val result1 = validator.validate(url1, constraint, context)
        val result2 = validator.validate(url2, constraint, context)
        val result3 = validator.validate(url3, constraint, context)
        
        // Then
        assertNull(result1)
        assertNull(result2)
        assertNotNull(result3)
        assertEquals("URL_INVALID_PORT", result3?.code)
    }
    
    @Test
    fun `validate should use custom messages when provided`() {
        // Given
        val constraint = UrlConstraint(
            groups = setOf(Any::class),
            messages = mapOf(
                "en" to "Please enter a valid website URL",
                "fr" to "Veuillez saisir une URL de site web valide"
            )
        )
        val value = "invalid-url"
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("Please enter a valid website URL", result?.message)
    }
    
    @Test
    fun `validate should handle null protocol setting`() {
        // Given
        val constraint = UrlConstraint(
            protocols = null,
            groups = setOf(Any::class)
        )
        val url = "http://example.com"
        
        // When
        val result = validator.validate(url, constraint, context)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validate should handle empty string`() {
        // Given
        val constraint = UrlConstraint(groups = setOf(Any::class))
        val value = ""
        
        // When
        val result = validator.validate(value, constraint, context)
        
        // Then
        assertNotNull(result)
        assertEquals("URL_INVALID", result?.code)
    }
}

