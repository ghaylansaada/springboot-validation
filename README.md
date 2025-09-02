# Spring Boot Schema-Based Validation

A high-performance, schema-based validation framework for Spring Boot applications that provides superior flexibility, performance, and developer experience compared to standard Bean Validation (JSR-380).

## Overview

This library is a complete validation solution for Spring Boot applications that improves upon the built-in validation mechanisms in several key ways:

- **Schema-based**: Generates optimized validation schemas at startup or runtime
- **High-performance**: Precomputes field accessors and avoids reflection in the hot path
- **AOP-driven**: Automatically validates controller inputs via simple annotation
- **API-friendly errors**: Structured, localized error responses suitable for REST APIs
- **Dynamic validation**: Supports runtime schema generation for dynamic payloads
- **Manual validation**: Powerful error collection API for custom validation logic

Perfect for applications with complex validation requirements, high performance needs, or API-first designs.

> **Note**: Currently, this framework is coroutine-first and primarily designed for Kotlin coroutines with Spring WebFlux. It also supports Spring WebFlux without coroutines, but traditional Spring WebMVC is not supported.

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Getting Started](#getting-started)
- [Usage Examples](#usage-examples)
- [Built-in Constraints](#built-in-constraints)
- [Error Handling](#error-handling)
- [Extending the Framework](#extending-the-framework)
- [Performance Considerations](#performance-considerations)
- [Why Use This Framework?](#why-use-this-framework)

## Features

- **Schema-based validation**: Pre-computed validation schemas for performance
- **Zero-reflection runtime**: Uses compiled accessors for field values
- **Lightweight and fast**: Optimized for high-throughput applications
- **Spring Boot integration**: Auto-configuration with minimal setup
- **Comprehensive constraints**: Rich set of built-in validators
- **Localized messages**: Multi-language error messages out of the box
- **Flexible API responses**: Structured error format for API clients
- **Dynamic validation**: Support for runtime schema generation
- **Manual validation mode**: Full control over validation flow when needed
- **Integration with WebFlux**: Support for reactive applications and Kotlin coroutines

## How It Works

The validation framework operates through a series of steps that span from application startup to runtime request handling. The core principle is to perform all expensive operations (reflection, annotation processing) at startup and use cached components during request validation for optimal performance.

### Startup Process: Caching Validators

When the application starts:

1. The framework scans the classpath for annotations marked with `@Constraint`.
2. For each constraint found, it:
    - Maps the annotation to its metadata class (specified in the `metadata` property)
    - Collects all validator classes from the `validatedBy` array
    - Obtains validator instances through:
        - Existing singletons (Kotlin objects)
        - Spring beans (`@Bean`, `@Component`)
        - Direct instantiation via empty constructor
3. Validators are cached in `ValidationRegistry` in a map structure where:
    - The outer key is the constraint metadata class type
    - The inner key is the value type the validator can handle
    - The value is the validator instance itself

This creates an optimized lookup system that can quickly match a constraint with the appropriate validator for any given value type.

### Startup Process: Building Request Schemas

After caching validators, the framework:

1. Finds all controller methods annotated with `@ValidateInput`
2. For each method, it analyzes:
    - The request body class and all nested fields
    - Path variables
    - Query parameters
    - HTTP headers
3. For each field, parameter, or property:
    - Extracts constraint annotations using reflection
    - Converts annotations to metadata objects
    - Matches with appropriate validator instances from the cache
    - Retrieves (or creates and caches) a field accessor from `AccessorRegistry`
    - Packages everything into a `PropertySpec` object
4. Creates a complete request schema containing:
    - Maps of all path variables, headers, query params, and request body fields
    - Links to their respective validators and field accessors
    - Validation configuration from the `@ValidateInput` annotation
5. Registers the schema in `ValidationRegistry`, indexed by a unique request ID

At this point, every endpoint has a complete validation schema with cached accessors, validators, and constraint metadata - all created without needing to access annotations or use reflection at runtime.

### Runtime Validation Process

When a request comes in:

1. The `ValidationAspect` intercepts calls to methods annotated with `@ValidateInput`
2. It extracts the request body, query parameters, headers, and path variables
3. Generates a unique ID for the controller method and retrieves its cached schema
4. Passes everything to the `ValidatorEngine` for validation
5. The engine systematically validates each enabled part of the request:
    - For each field in the schema, it:
        - Retrieves the field's value using the cached accessor (no reflection)
        - Applies each constraint by invoking its cached validator with its metadata
        - If validation fails, collects an error with field path, code, and message
    - For nested objects or collections, recursively validates using nested schema parts
6. If any errors are found, throws a `ConstraintViolationException` with all errors
7. If validation passes, allows the controller method to execute normally

This approach ensures validation is performed with minimal overhead, as all expensive operations were moved to the application startup phase. During actual request processing, the framework simply walks through pre-computed validation paths and applies pre-instantiated validators.

## Getting Started

### Add Dependency

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.ghaylan.springboot:validation:1.0.0")
}
```

#### Maven

```xml
<dependency>
    <groupId>io.ghaylan.springboot</groupId>
    <artifactId>validation</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Enable Auto-configuration

The library auto-configures itself when added to a Spring Boot application. Just add the dependency, and it will:

1. Register all necessary beans
2. Set up the validation aspect
3. Configure exception handlers
4. Build validation schemas at startup

No additional configuration is required - the framework is designed to work out of the box with sensible defaults.

## Usage Examples

### 1. Automatic Validation with @ValidateInput

The `@ValidateInput` annotation provides several configuration options:

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateInput(
    val validateBody: Boolean = true,        // Whether to validate request body
    val validateQuery: Boolean = true,       // Whether to validate query parameters
    val validatePath: Boolean = true,        // Whether to validate path variables
    val validateHeaders: Boolean = true,     // Whether to validate request headers
    val singleErrorPerField: Boolean = true, // Stop after first error on a field
    val groups: Array<KClass<*>> = [DefaultGroup::class]) // Validation groups to apply
```

Example usage in a controller:

```kotlin
@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    @ValidateInput(
        validateBody = true,
        validateQuery = true,
        validateHeaders = false,
        singleErrorPerField = true,
        groups = [CreateGroup::class])
    suspend fun createUser(@RequestBody user: UserDTO): UserDTO {
        return userService.createUser(user)
    }

    @PutMapping("/{id}")
    @ValidateInput(groups = [UpdateGroup::class])
    suspend fun updateUser(
        @PathVariable id: Long,
        @RequestBody user: UserDTO
    ): UserDTO {
        return userService.updateUser(id, user)
    }
}
```

### Validation Groups

Validation groups allow you to apply different validation rules depending on the context:

```kotlin
data class UserDTO(
    // Applied in both create and update
    @Required(groups = [CreateGroup::class, UpdateGroup::class])
    val id: Long?,

    // Only required when creating
    @Required(groups = [CreateGroup::class])
    @Email(groups = [CreateGroup::class, UpdateGroup::class])
    val email: String?,

    // Required and validated only when creating
    @Required(groups = [CreateGroup::class])
    @StringLength(min = 8, max = 100, groups = [CreateGroup::class])
    val password: String?)
```

Built-in groups:
- `DefaultGroup`: Default group used when no group is specified
- `CreateGroup`: Commonly used for creation operations
- `UpdateGroup`: Commonly used for update operations

You can also create custom groups by defining marker interfaces:

```kotlin
interface AdminGroup
interface ProfileGroup
```

### 2. Dynamic Validation (Runtime Schema)

For cases when you need to validate objects dynamically (e.g., user-uploaded schemas):

```kotlin
@Service
class DynamicValidationService(private val validatorEngine: ValidatorEngine) {

    suspend fun validateDynamicObject(data: Any, locale: Locale = Locale.getDefault()) {
        // Dynamically validate any object - schema will be created on demand
        validatorEngine.validate(
            params = data,
            locale = locale,
            singleErrorPerField = false,
            groups = arrayOf(DefaultGroup::class))
    }
}
```

### 3. Manual Validation with ApiErrorCollector

For complex validation logic or business rules that can't be expressed with annotations:

```kotlin
@Service
class OrderService(private val repository: OrderRepository) {

    suspend fun placeOrder(order: OrderDTO, locale: Locale): Order {
        val collector = ApiErrorCollector(locale)

        // We can use collector.body { … }, collector.header { … }, collector.path { … }, 
        // collector.query { … }, collector.business { … } to add errors at different locations

        // Validate business rules with BUSINESS error location
        if (order.items.isEmpty()) {
            // Add error code (Must be an enum)
            collector.business()
                .code(ErrorCodes.ORDER_EMPTY)
                .data("order_id", order.id)
                .msgEnglish("Order must contain at least one item")
                .msgFrench("La commande doit contenir au moins un article")
                .msg("es", "El pedido debe contener al menos un artículo")
        }

        // Throw if any errors were collected
        collector.throwIfNotEmpty()

        // Process the order if valid
        return repository.saveOrder(order)
    }
}
```

## Common Constraint Properties

All constraints support these common properties:

### Groups

Validation groups control when constraints are applied:

```kotlin
@field:Required(groups = [CreateGroup::class, UpdateGroup::class])
val name: String?
```

### Messages

Define localized error messages for constraints:

```kotlin
@field:Email(messages = [
    ErrorMessage(lang = "en", text = "Invalid email format"),
    ErrorMessage(lang = "fr", text = "Format d'email invalide"),
    ErrorMessage(lang = "de", text = "Ungültiges E-Mail-Format")])
val email: String?
```

Language codes can be specified as:
- Language only: `"en"`, `"fr"`, `"de"`, etc.
- Language with region: `"en-US"`, `"fr-CA"`, `"de-CH"`, etc.

If no custom message is provided, the framework falls back to default English messages created during validation.

## Built-in Constraints

The framework provides a rich set of built-in constraints:

### General Constraints

| Constraint | Description |
|------------|-------------|
| `@Required` | Ensures a value is not null or empty |

### String Constraints

| Constraint | Description |
|------------|-------------|
| `@StringLength` | Validates string length is between min and max |
| `@Email` | Validates string is a valid email address |
| `@Regex` | Validates string matches a regular expression pattern |
| `@StrOcc` | Validates string contains specific text |
| `@Base64` | Validates string is valid Base64 encoded |
| `@Url` | Validates string is a valid URL |
| `@Uuid` | Validates string is a valid UUID |
| `@HexColor` | Validates string is a valid hex color code |
| `@Html` | Validates string contains valid HTML |
| `@Iban` | Validates string is a valid IBAN |
| `@ISOCountryCode` | Validates string is a valid ISO country code |
| `@ISOLanguage` | Validates string is a valid ISO language code |
| `@Phone` | Validates string is a valid phone number |
| `@CreditCard` | Validates string is a valid credit card number |
| `@Password` | Validates string meets password strength requirements |

### Number Constraints

| Constraint | Description |
|------------|-------------|
| `@NumberMin` | Validates number is at least the specified minimum |
| `@NumberMax` | Validates number is at most the specified maximum |
| `@DivisibleBy` | Validates number is divisible by the specified value |
| `@MultipleOf` | Validates number is a multiple of the specified value |
| `@Latitude` | Validates number is a valid latitude (-90 to 90) |
| `@Longitude` | Validates number is a valid longitude (-180 to 180) |

### Comparison Constraints

| Constraint | Description |
|------------|-------------|
| `@EqualTo` | Validates value equals specified value |
| `@NotEqualTo` | Validates value does not equal specified value |
| `@GreaterThan` | Validates value is greater than specified value |
| `@LessThan` | Validates value is less than specified value |
| `@ValueIn` | Validates value is in a set of allowed values |
| `@ValueNotIn` | Validates value is not in a set of disallowed values |

### Collection Constraints

| Constraint | Description |
|------------|-------------|
| `@ArraySize` | Validates array/collection size is between min and max |
| `@Distinct` | Validates array/collection has no duplicate elements |
| `@MapSize` | Validates map has number of entries between min and max |

### Temporal Constraints

| Constraint | Description |
|------------|-------------|
| `@Past` | Validates date/time is in the past |
| `@Future` | Validates date/time is in the future |
| `@TemporalMin` | Validates date/time is after specified minimum |
| `@TemporalMax` | Validates date/time is before specified maximum |
| `@AllowedDays` | Validates date falls on allowed days of week |

## Error Handling

### Error Structure

Validation errors are represented as `ApiError` objects with the following structure:

```kotlin
data class ApiError(
    // Field path (e.g., "user.address.city" or "items[0].quantity")
    val field: String?,

    // Error code (e.g., ApiErrorCode.REQUIRED, CustomEnum.EMAIL_INVALID)
    val code: Enum<*>?,

    // Error location (BODY, QUERY, HEADER, PATH, BUSINESS)
    val location: ErrorLocation?,

    // Localized error message
    val message: String?,

    // Additional error data (e.g., {"min": 5, "actual": 3})
    val data: Any?)
```

The `location` property helps categorize errors by source:
- `BODY`: Request body errors
- `QUERY`: Query parameter errors
- `HEADER`: HTTP header errors
- `PATH`: Path variable errors
- `BUSINESS`: Business logic errors

### Field Path Syntax

The `field` property in `ApiError` represents the path to the invalid value within the request payload.  
It uses **dot-and-bracket notation**, similar to JSONPath, and supports both object properties and array indices.

#### Example

| Field Path             | Meaning                                                |
|------------------------|--------------------------------------------------------|
| `field`                | Root object property                                   |
| `field.field`          | Nested property inside an object                       |
| `field[0]`             | First element of an array                              |
| `field[0].field`       | Field inside the first array element                   |
| `field[0][0][0].field` | Deeply nested array element with a field               |
| `[0].field`            | Field inside the first element of a root-level array   |
| `[0][0].field`         | Field inside nested arrays at the root level           |
| `field[0]`             | First element of an array under a specific field       |

### Exception Handling

`ConstraintViolationException` is a runtime exception thrown when validation fails. It contains a list of `ApiError` objects.

Since this is a runtime exception, you need to handle it in a custom exception handler to map it to your API response format:

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed",
            errors = ex.errors)
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<ApiError>)
```

### Error Collection Flow

1. **Error Detection**: Validators detect constraint violations during validation
2. **Error Creation**: Violations are converted to `ApiError` instances with:
    - Full field path for precise location
    - Standard error code for programmatic handling
    - Localized message based on user locale
    - Additional context data when relevant
3. **Error Collection**: All errors are collected in a list during validation
4. **Deduplication**: Duplicate errors are removed based on field/code/location
5. **Exception Throwing**: `ConstraintViolationException` wraps all errors
6. **Response Conversion**: Your exception handler converts to HTTP response

### Localized Messages

The framework provides multi-language support for error messages:

```kotlin
// Define messages in multiple languages
collector.body()
    .code(ErrorCodes.INVALID_EMAIL)
    .field("email")
    .msgEnglish("Invalid email address")
    .msgFrnech("Adresse email invalide")
    .msg("de", "Ungültige E-Mail-Adresse")

// Messages are resolved based on user's locale
collector.throwIfNotEmpty()
```

## Extending the Framework

### Creating Custom Constraints

1. Define the constraint annotation:

```kotlin
@Constraint(
    metadata = MyCustomConstraintMetadata::class,
    validatedBy = [
        MyCustomStringValidator::class,
        MyCustomNumberValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MyCustomConstraint(
    val customValue: String,
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages: Array<ErrorMessage> = [])
```

2. Create the constraint metadata (must match annotation properties):

```kotlin
class MyCustomConstraintMetadata(
    val customValue: String,
    override val groups: Set<KClass<*>>,
    override val messages: Map<String, String>? = null
) : ConstraintMetadata {
    override val appliesToContainer = false
}
```

3. Implement validators for different types:

```kotlin
// String validator
object MyCustomStringValidator : ConstraintValidator<String, MyCustomConstraintMetadata> {
    // IMPORTANT: Validators MUST be stateless/singleton
    // Don't use class variables to store state!
    
    override suspend fun validate(value: String?, metadata: MyCustomConstraintMetadata, context: ValidationContext): ApiError? {
        if (value == null) return null
        
        // Custom validation logic here
        if (!isValid(value, metadata.customValue)) {
            // Return specific error code and message, 
            // base validator class `ConstraintValidator` will fill in the rest.
            return ApiError(
                code = ErrorCode.CUSTOM_CONSTRAINT_FAILED, 
                message = "Value doesn't meet custom constraint",
                data = mapOf("actual" to value, "expected" to metadata.customValue)) // Optional data
        }
        
        return null
    }
    
    private fun isValid(value: String, customValue: String): Boolean {
        // Your custom validation logic
        return true
    }
}

// Number validator for the same constraint
class MyCustomNumberValidator : ConstraintValidator<Number, MyCustomConstraintMetadata> {
    // Handle number validation logic
    // ...
}
```

Validators can be Spring beans to access services or repositories:

```kotlin
@Component
class DatabaseBackedValidator(
    private val repository: SomeRepository
) : ConstraintValidator<String, MyCustomConstraintMetadata> {
    override suspend fun validate(value: String?, metadata: MyCustomConstraintMetadata, context: ValidationContext): ApiError? {
        // Access repository or other services
        val isValid = repository.validateSomething(value)
        // ...
    }
}
```

> **Important Notes:**
> - Constraints are auto-registered - no manual registration is needed
> - The framework discovers all `@Constraint` annotations, instantiates validators, and generates metadata
> - Validators MUST be stateless as they are shared across requests
> - Include `groups` and `messages` in your constraint annotations for consistency
> - The metadata class should match the annotation properties for automatic mapping

## Performance Considerations

This validation framework is designed for high performance:

### Precomputed Schema

- **One-time analysis**: Field scanning and constraint resolution happen only once at startup
- **Cached schemas**: Validation schemas are built once and reused for all requests
- **No reflection at runtime**: Field access is performed through precomputed accessors

### Fast Field Access

- **Accessor Registry**: Caches fast field access strategies
- **Optimized strategies**:
    - VarHandle (Java 9+): Direct memory access, JIT-friendly
    - MethodHandle: Fast reflection alternative
    - Standard reflection: Only as fallback
    - Map lookup: For header/query/param maps

### Constraint Optimization

- **Metadata Caching**: Annotations are mapped to metadata objects once
    - Avoids repeated reflection-based annotation reading at runtime
    - Allows fast access to constraint properties during validation
    - Enables more efficient constraint validation logic

### Validation Optimizations

- **Fail-fast mode**: Stops validation on first error per field
- **Constraint ordering**: Required constraints checked first
- **Zero-copy collections**: Minimizes object creation during array normalization
- **Path building**: Efficient string concatenation for field paths
- **Error deduplication**: Eliminates duplicate errors

### Performance Benefits

- **Lower latency**: Faster validation execution compared to standard Bean Validation
- **Reduced memory usage**: Less garbage collection pressure
- **Better throughput**: Can handle more requests per second
- **Predictable performance**: Consistent validation times

## Why Use This Framework?

### Advantages over Spring Boot's Built-in Validation

| Feature | This Framework | Standard Bean Validation |
|---------|---------------|-------------------------|
| **Performance** | Optimized field access, no runtime reflection | Relies on reflection for each validation |
| **Schema Generation** | Pre-computed validation schemas | Schema discovery on each validation |
| **Error Structure** | API-friendly, structured errors | Limited customization of error format |
| **Dynamic Validation** | First-class support | Difficult to implement |
| **Localization** | Built-in multi-language support | Requires custom MessageSource configuration |
| **Validation Groups** | First-class support | Available but with limitations |
| **Manual Error Collection** | Rich fluent API | Limited programmatic API |
| **Extensibility** | Easy custom constraints | More complex extension model |
| **WebFlux Support** | Native support for reactive & coroutines | Limited reactive integration |

### When to Use This Framework

- **High-performance applications**: When validation speed matters
- **API-first designs**: For consistent, structured error responses
- **Complex validation rules**: When you need flexible constraint composition
- **Dynamic payloads**: When validating structures not known at compile time
- **Multi-language applications**: For easy localization of error messages
- **Reactive applications**: When using Spring WebFlux with or without coroutines

### Use Cases

- **API backends**: Clean, structured validation errors for clients
- **High-throughput systems**: Optimized performance for many requests
- **Complex domain models**: Rich constraint composition for business rules
- **Multi-language applications**: Easy error message localization
- **Dynamic data processing**: Validation of user-defined schemas
- **Reactive microservices**: Non-blocking validation for WebFlux applications

## License

### MIT License

Copyright (c) 2025 Ghaylan Saada
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE