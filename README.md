# Spring Boot Schema-Based Validation

A high-performance, schema-based validation framework for Spring Boot applications that provides superior flexibility,
performance, and developer experience compared to standard Bean Validation (JSR-380).

## Overview

This library is a complete validation solution for Spring Boot applications that improves upon the built-in validation
mechanisms in several key ways:

- **Schema-based**: Generates optimized validation schemas at startup or runtime
- **High-performance**: Precomputes field accessors and avoids reflection in the hot path
- **AOP-driven**: Automatically validates controller inputs via simple annotation
- **API-friendly errors**: Structured error responses with error codes, field paths, and locations
- **Dynamic validation**: Supports runtime schema generation for dynamic payloads
- **Manual validation**: Powerful error collection API for custom validation logic

Perfect for applications with complex validation requirements, high performance needs, or API-first designs.

> **Note**: This framework is coroutine-first and primarily designed for Kotlin coroutines with Spring WebFlux.
> It also supports Spring WebFlux without coroutines (`Mono`/`Flux`), but traditional Spring WebMVC is not supported.

## Requirements

- **JDK**: 25+
- **Kotlin**: 2.3+
- **Spring Boot**: 4.0+
- **Spring WebFlux** (required — WebMVC is not supported)

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Getting Started](#getting-started)
- [Usage Examples](#usage-examples)
- [Built-in Constraints](#built-in-constraints)
- [Constraint Reference](#constraint-reference)
- [Error Handling](#error-handling)
- [Extending the Framework](#extending-the-framework)
- [Performance Considerations](#performance-considerations)
- [Why Use This Framework?](#why-use-this-framework)

## Features

- **Schema-based validation**: Pre-computed validation schemas for performance
- **Zero-reflection runtime**: Uses compiled accessors (VarHandle, MethodHandle) for field values
- **Lightweight and fast**: Optimized for high-throughput applications
- **Spring Boot auto-configuration**: Zero-config setup with `@AutoConfiguration`
- **45+ built-in constraints**: Rich set of validators covering strings, numbers, temporals, collections, and more
- **Flexible API responses**: Structured `ApiError` format with field paths, error codes, and locations
- **Dynamic validation**: Support for runtime schema generation for arbitrary classes
- **Manual validation mode**: Fluent `ApiErrorCollector` API for business rule validation
- **Validation groups**: Context-sensitive rules with `OnCreate`, `OnUpdate`, `OnDefault`, or custom groups
- **Cross-field validation**: Compare properties within the same object (`@EqualTo`, `@GreaterThan`, `@LessThan`, etc.)
- **Conditional requirements**: `@Required` with `IF_DEPENDENT_NULL` / `IF_DEPENDENT_NOT_NULL` conditions
- **Nested & recursive validation**: Deep validation of nested objects, arrays, and multi-dimensional collections
- **WebFlux integration**: Native support for `Mono`, `Flux`, `Flow`, and `suspend` functions

## How It Works

The validation framework operates through a series of steps that span from application startup to runtime request
handling. The core principle is to perform all expensive operations (reflection, annotation processing) at startup and
use cached components during request validation for optimal performance.

### Startup Phase 1: Discovering Validators

When the application starts:

1. The framework scans the classpath for annotations marked with `@Constraint`.
2. For each constraint found, it:
    - Maps the annotation to its metadata class (specified in the `metadata` property)
    - Collects all validator classes from the `validatedBy` array
    - Obtains validator instances through (in order):
        1. Kotlin `object` singleton
        2. Spring-managed bean
        3. Autowiring via `AutowireCapableBeanFactory`
        4. No-arg constructor fallback
3. Validators are cached in `ValidationRegistry` in a nested map:
    - **Outer key**: constraint metadata class (`KClass<out ConstraintMetadata>`)
    - **Inner key**: supported value type (`TypeInfo`)
    - **Value**: validator instance

### Startup Phase 2: Building Request Schemas

After caching validators, the framework:

1. Finds all controller methods annotated with `@ValidateInput`
2. For each method, analyzes all parameter annotations:
    - `@RequestBody` — request body class and all nested fields
    - `@PathVariable` — path variables
    - `@RequestParam` — query parameters
    - `@RequestHeader` — HTTP headers
3. For each field or parameter:
    - Extracts constraint annotations and converts them to `ConstraintMetadata` via `ConstraintConverter`
    - Matches each constraint with the most compatible validator (exact type, supertype, wildcard, or `Any`)
    - Builds or retrieves a cached `FieldAccessor` from `AccessorRegistry`
    - Packages everything into a `PropertySpec`
4. Creates a complete `RequestInputSchema` containing maps of all path variables, headers, query params, and body fields
5. Registers the schema in `ValidationRegistry`, indexed by a unique method identifier

### Runtime Validation

When a request arrives:

1. `ValidationAspect` intercepts methods annotated with `@ValidateInput`
2. `WebFluxValidationHandler` resolves reactive parameters (`Mono`, `Flux`, `Flow`) non-blockingly
3. `ValidatorEngine` validates each enabled section (body, query, headers, path):
    - Retrieves field values using cached accessors (no reflection)
    - Applies each constraint via cached validators
    - Builds precise field paths (`user.address[0].city`)
    - Collects `ApiError` instances with codes, paths, and optional messages
4. Deduplicates errors and throws `ConstraintViolationException` if any violations exist

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

### Auto-Configuration

The library auto-configures itself via Spring Boot's `@AutoConfiguration`. Adding the dependency automatically
registers:

- `ServerWebExchangeContextFilter` — reactive web context access
- `ValidationRegistry` — validator and schema cache
- `ValidatorEngine` — validation execution engine
- `ValidationAspect` — AOP-based validation interceptor

All beans use `@ConditionalOnMissingBean`, so you can override any component by providing your own bean.

## Usage Examples

### 1. Automatic Validation with @ValidateInput

The `@ValidateInput` annotation enables automatic validation on controller methods:

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateInput(
    val validateBody: Boolean = true,
    val validateQuery: Boolean = true,
    val validatePath: Boolean = true,
    val validateHeaders: Boolean = true,
    val singleErrorPerField: Boolean = true,
    val groups: Array<KClass<*>> = [OnDefault::class]
)
```

Example controller:

```kotlin
@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    @ValidateInput(groups = [OnCreate::class])
    suspend fun createUser(@RequestBody user: UserDTO): UserDTO {
        return userService.createUser(user)
    }

    @PutMapping("/{id}")
    @ValidateInput(groups = [OnUpdate::class], validateHeaders = false)
    suspend fun updateUser(
        @PathVariable id: Long,
        @RequestBody user: UserDTO
    ): UserDTO {
        return userService.updateUser(id, user)
    }
}
```

### Validation Groups

Groups allow different validation rules depending on the context:

```kotlin
data class UserDTO(
    @field:Required(groups = [OnCreate::class, OnUpdate::class])
    val id: Long?,

    @field:Required(groups = [OnCreate::class])
    @field:Email(groups = [OnCreate::class, OnUpdate::class])
    val email: String?,

    @field:Required(groups = [OnCreate::class])
    @field:TextLength(min = 8, max = 100, groups = [OnCreate::class])
    @field:Password(
        requireUppercase = true, requireDigit = true,
        groups = [OnCreate::class])
    val password: String?,

    @field:EqualTo(property = "password", groups = [OnCreate::class])
    val confirmPassword: String?
)
```

Built-in groups:

- `OnDefault` — default group, used when no group is specified
- `OnCreate` — for creation operations
- `OnUpdate` — for update operations

Create custom groups by defining marker interfaces:

```kotlin
interface OnAdmin
interface OnPublic
```

### 2. Dynamic Validation (Runtime Schema)

Validate arbitrary objects at runtime — schemas are created and cached on demand:

```kotlin
@Service
class DynamicValidationService(private val validatorEngine: ValidatorEngine) {

    suspend fun validateObject(data: Any) {
        validatorEngine.validate(
            params = data,
            singleErrorPerField = false,
            groups = arrayOf(OnDefault::class)
        )
    }
}
```

### 3. Manual Validation with ApiErrorCollector

For business rules that can't be expressed with annotations:

```kotlin
@Service
class OrderService(private val repository: OrderRepository) {

    suspend fun placeOrder(order: OrderDTO): Order {
        val collector = ApiErrorCollector()

        if (order.items.isEmpty()) {
            collector.business(OrderErrorCode.ORDER_EMPTY)
                .data("order_id", order.id)
                .message("Order must contain at least one item")
        }

        if (order.total < 0) {
            collector.body(OrderErrorCode.INVALID_TOTAL)
                .field("total")
                .message("Total cannot be negative")
        }

        // Throws ConstraintViolationException if any errors collected
        collector.throwIfNotEmpty()

        return repository.saveOrder(order)
    }
}
```

The collector supports all error locations: `body()`, `query()`, `header()`, `path()`, `business()`.

Each error builder supports: `.field()`, `.data()`, and `.message()`.

## Common Constraint Properties

All constraints share these properties:

### groups

Controls when the constraint is applied:

```kotlin
@field:Required(groups = [OnCreate::class, OnUpdate::class])
val name: String?
```

### message

Optional error message override:

```kotlin
@field:Email(message = "Please enter a valid email address")
val email: String?
```

If not set, the validator provides a default message.

## Built-in Constraints

### General

| Constraint   | Description | Key Properties |
|--------------|-------------|----------------|
| `@Required`  | Ensures a value is not null, empty, or blank | `dependentField`, `condition` |

### String

| Constraint       | Description | Key Properties |
|------------------|-------------|----------------|
| `@TextLength`    | String length within min/max bounds | `min`, `max` |
| `@Email`         | Valid email address format | — |
| `@Regex`         | Matches a regular expression | `pattern` |
| `@StrOcc`        | String contains/equals/starts/ends with a value | `value`, `mode`, `minOccurrences`, `maxOccurrences`, `ignoreCase` |
| `@NotStrOcc`     | String must NOT contain/equal/start/end with a value | `value`, `mode`, `ignoreCase` |
| `@Base64`        | Valid Base64 encoded string | — |
| `@Url`           | Valid URL with type/protocol/extension validation | `type`, `requireHttps`, `allowQueryParams`, `allowedExtensions` |
| `@Uuid`          | Valid UUID format | — |
| `@HexColor`      | Valid hex color code (`#FFF` or `#FFFFFF`) | — |
| `@Html`          | Safe HTML with tag/attribute/protocol whitelisting | `allowedTags`, `allowedAttrs`, `allowedProtocols` |
| `@IBAN`          | Valid International Bank Account Number | — |
| `@ISOCountryCode`| Valid ISO 3166-1 alpha-2 country code | — |
| `@CurrencyCode`  | Valid ISO 4217 currency code | — |
| `@LanguageCode`  | Valid ISO 639-1 language code (e.g., `"en"`, `"fr"`) | — |
| `@Phone`         | Valid international phone number | `allowedTypes`, `allowedCountries` |
| `@CreditCard`    | Valid credit card number (Luhn algorithm) | — |
| `@Password`      | Configurable password strength validation | `minLength`, `maxLength`, `requireUppercase`, `requireLowercase`, `requireDigit`, `requireSpecialChar`, `allowedSpecialChars`, `minEntropy` |
| `@Enum`          | Matches enum naming style (`UPPER_SNAKE_CASE`) | `ignoreCase` |
| `@NBR`           | Valid National Business Registry number | — |

### Number

| Constraint     | Description | Key Properties |
|----------------|-------------|----------------|
| `@NumberMin`   | Minimum numeric value | `value`, `inclusive` |
| `@NumberMax`   | Maximum numeric value | `value`, `inclusive` |
| `@DivisibleBy` | Divisible by a given divisor | `divisor` |
| `@MultipleOf`  | Multiple of a given factor | `factor` |
| `@Latitude`    | Valid latitude (−90 to 90) | — |
| `@Longitude`   | Valid longitude (−180 to 180) | — |

### Cross-Field Comparison

These constraints compare the annotated field against another field **in the same object** using the `property` parameter:

| Constraint     | Description | Key Properties |
|----------------|-------------|----------------|
| `@EqualTo`     | Must equal the referenced property | `property` |
| `@NotEqualTo`  | Must not equal the referenced property | `property` |
| `@GreaterThan` | Must be greater than the referenced property | `property`, `inclusive` |
| `@LessThan`    | Must be less than the referenced property | `property`, `inclusive` |
| `@ValueIn`     | Value must be in a set of allowed string values | `values` |
| `@ValueNotIn`  | Value must not be in a set of disallowed string values | `values` |

```kotlin
data class DateRange(
    val startDate: LocalDate?,

    @field:GreaterThan(property = "startDate", inclusive = false)
    val endDate: LocalDate?
)
```

### Collection

| Constraint   | Description | Key Properties |
|--------------|-------------|----------------|
| `@ArraySize` | Array/collection size within min/max bounds | `min`, `max` |
| `@Distinct`  | Elements must be unique (by field or combination) | `by`, `mode` |
| `@MapSize`   | Map entry count within min/max bounds | `min`, `max` |

### Temporal

| Constraint     | Description | Key Properties |
|----------------|-------------|----------------|
| `@Past`        | Must be in the past (with optional within-range) | `withinDays`, `withinHours`, `withinMinutes`, ... |
| `@Future`      | Must be in the future (with optional within-range) | `withinDays`, `withinHours`, `withinMinutes`, ... |
| `@TemporalMin` | Must be after a specified date/time | `value`, `inclusive` |
| `@TemporalMax` | Must be before a specified date/time | `value`, `inclusive` |
| `@AllowedDays` | Must fall on specified days of the week | `days` |

Supported temporal types: `LocalDate`, `LocalTime`, `OffsetTime`, `LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`, `Instant`.

## Constraint Reference

### @Required — Conditional Dependencies

```kotlin
data class ContactDTO(
    val email: String?,

    // Required only when email is not provided
    @field:Required(dependentField = "email", condition = RequirementCondition.IF_DEPENDENT_NULL)
    val phone: String?,

    // Required only when email IS provided
    @field:Required(dependentField = "email", condition = RequirementCondition.IF_DEPENDENT_NOT_NULL)
    val emailVerificationCode: String?
)
```

Conditions: `ALWAYS` (default), `IF_DEPENDENT_NULL`, `IF_DEPENDENT_NOT_NULL`.

### @Password — Strength Configuration

```kotlin
@field:Password(
    minLength = 12,
    maxLength = 128,
    requireUppercase = true,
    requireLowercase = true,
    requireDigit = true,
    requireSpecialChar = true,
    allowedSpecialChars = "!@#$%^&*",
    minEntropy = Password.PasswordStrength.STRONG
)
val password: String?
```

Strength levels (Shannon entropy bits): `VERY_WEAK` (0), `WEAK` (28), `MODERATE` (36), `STRONG` (60), `VERY_STRONG` (128).

### @Url — Type-Specific Validation

```kotlin
@field:Url(type = UrlType.IMAGE, requireHttps = true)
val avatarUrl: String?

@field:Url(type = UrlType.WEBSITE, allowQueryParams = false)
val homepage: String?

@field:Url(type = UrlType.VIDEO, allowedExtensions = ["mp4", "webm"])
val videoUrl: String?
```

URL types: `GENERIC`, `WEBSITE`, `FILE`, `IMAGE`, `VIDEO`, `AUDIO`. Media types enforce valid file extensions.

### @Future / @Past — Within-Range Constraints

```kotlin
// Must be in the future, but within the next 30 days
@field:Future(withinDays = 30)
val appointmentDate: LocalDateTime?

// Must be in the past, within the last 1 year
@field:Past(withinYears = 1)
val dateOfBirth: LocalDate?
```

Range parameters: `withinSeconds`, `withinMinutes`, `withinHours`, `withinDays`, `withinWeeks`, `withinMonths`, `withinYears`.

### @Distinct — Uniqueness Modes

```kotlin
// Scalar uniqueness
@field:Distinct
val tags: List<String>?

// Unique by a single field
@field:Distinct(by = ["email"], mode = DistinctMode.PER_FIELD)
val users: List<UserDTO>?

// Unique by combination of fields
@field:Distinct(by = ["firstName", "lastName"], mode = DistinctMode.COMBINATION)
val employees: List<EmployeeDTO>?
```

Modes: `PER_FIELD` (each field independently unique), `COMBINATION` (unique by tuple of all specified fields).

### @Html — Safe HTML Whitelisting

```kotlin
@field:Html(
    allowedTags = ["b", "i", "u", "p", "a", "ul", "li"],
    allowedAttrs = ["a:href"],
    allowedProtocols = ["https"]
)
val richText: String?
```

### @Phone — Type and Country Filtering

```kotlin
@field:Phone(
    allowedTypes = [PhoneNumberUtil.PhoneNumberType.MOBILE],
    allowedCountries = ["US", "FR", "TN"]
)
val mobilePhone: String?
```

### @StrOcc — String Occurrence Modes

```kotlin
@field:StrOcc(value = "http", mode = StrOccMode.STARTS_WITH)
val url: String?

@field:StrOcc(value = "@", mode = StrOccMode.CONTAINS, minOccurrences = 1, maxOccurrences = 1)
val email: String?

@field:NotStrOcc(value = "admin", mode = StrOccMode.CONTAINS, ignoreCase = true)
val username: String?
```

Modes: `EQUALS`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`.

## Error Handling

### Error Structure

Validation errors are represented as `ApiError`:

```kotlin
data class ApiError(
    val path: String?,            // Field path: "user.address[0].city"
    val code: Enum<*>?,           // Error code: ApiErrorCode.REQUIRED_VIOLATION
    var message: String?,         // Optional message: "Required"
    val location: ErrorLocation?, // BODY, QUERY, HEADER, PATH, BUSINESS
    val data: Any?                // Optional context data
)
```

### Error Locations

| Location   | Description |
|------------|-------------|
| `BODY`     | Request body field errors |
| `QUERY`    | Query parameter errors |
| `HEADER`   | HTTP header errors |
| `PATH`     | Path variable errors |
| `BUSINESS` | Business logic errors (manual validation) |

### Field Path Syntax

| Path                   | Meaning |
|------------------------|---------|
| `field`                | Root object property |
| `field.nested`         | Nested property |
| `field[0]`             | Array element |
| `field[0].name`        | Property inside array element |
| `field[0][1].name`     | Nested arrays |
| `[0].name`             | Root-level array element |

### Error Codes

All built-in error codes are in the `ApiErrorCode` enum:

```
REQUIRED_VIOLATION, EQUALITY_VIOLATION, INEQUALITY_VIOLATION,
GREATER_THAN_VIOLATION, LESS_THAN_VIOLATION, MIN_VALUE_VIOLATION,
MAX_VALUE_VIOLATION, ALLOWED_VALUE_VIOLATION, DISALLOWED_VALUE_VIOLATION,
DIVISIBILITY_VIOLATION, MULTIPLICITY_VIOLATION, LATITUDE_VIOLATION,
LONGITUDE_VIOLATION, PAST_VIOLATION, FUTURE_VIOLATION, DAY_OF_WEEK_VIOLATION,
BASE64_VIOLATION, ISO_COUNTRY_CODE_VIOLATION, ISO_CURRENCY_CODE_VIOLATION,
ISO_LANGUAGE_CODE_VIOLATION, CREDIT_CARD_PATTERN_VIOLATION,
EMAIL_FORMAT_VIOLATION, UUID_PATTERN_VIOLATION, URL_VIOLATION,
URL_HTTPS_REQUIRED_VIOLATION, URL_QUERY_PARAMS_NOT_ALLOWED_VIOLATION,
URL_EXTENSION_VIOLATION, URL_TYPE_VIOLATION, HTML_TAG_VIOLATION,
HTML_VALUE_VIOLATION, HTML_ATTRIBUTE_VIOLATION, HTML_PROTOCOL_VIOLATION,
STRING_OCCURRENCES_VIOLATION, PATTERN_VIOLATION, PHONE_FORMAT_VIOLATION,
PHONE_TYPE_VIOLATION, PHONE_COUNTRY_VIOLATION, NBR_FORMAT_VIOLATION,
IBAN_FORMAT_VIOLATION, HEX_COLOR_CODE_FORMAT_VIOLATION, ENUM_FORMAT_VIOLATION,
PASSWORD_LENGTH_VIOLATION, PASSWORD_UPPERCASE_VIOLATION,
PASSWORD_LOWERCASE_VIOLATION, PASSWORD_DIGIT_VIOLATION,
PASSWORD_SPECIAL_CHAR_VIOLATION, PASSWORD_ENTROPY_VIOLATION,
STRING_LENGTH_VIOLATION, OBJECT_SIZE_VIOLATION, ARRAY_SIZE_VIOLATION,
DISTINCT_VALUE_VIOLATION, DEPENDENCY_TYPE_VIOLATION
```

### Exception Handling

`ConstraintViolationException` is thrown when validation fails. Handle it in a `@RestControllerAdvice`:

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = 400,
                message = "Validation failed",
                errors = ex.errors
            )
        )
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<ApiError>
)
```

## Extending the Framework

### Creating Custom Constraints

#### 1. Define the Annotation

```kotlin
@Constraint(
    metadata = SlugConstraint::class,
    validatedBy = [SlugValidator::class]
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slug(
    val maxLength: Int = 100,
    val groups: Array<KClass<*>> = [OnDefault::class],
    val message: String = ""
)
```

#### 2. Create the Metadata Class

Property names must match the annotation properties exactly. The framework uses `ConstraintConverter` to
automatically map annotation values to the metadata constructor.

```kotlin
data class SlugConstraint(
    val maxLength: Int,
    override val groups: Set<KClass<*>>,
    override val message: String
) : ConstraintMetadata()
```

#### 3. Implement the Validator

```kotlin
object SlugValidator : ConstraintValidator<CharSequence, SlugConstraint>() {

    private val SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$")

    override suspend fun validate(
        value: CharSequence?,
        constraint: SlugConstraint,
        context: ValidationContext
    ): ApiError? {
        value ?: return null

        if (value.length > constraint.maxLength) {
            return ApiError(
                code = ApiErrorCode.STRING_LENGTH_VIOLATION,
                message = "Slug must be at most ${constraint.maxLength} characters"
            )
        }

        if (!SLUG_PATTERN.matcher(value).matches()) {
            return ApiError(
                code = ApiErrorCode.PATTERN_VIOLATION,
                message = "Must be a valid URL slug (lowercase, hyphens only)"
            )
        }

        return null
    }

    override fun applicableErrorCodes(): Array<ApiErrorCode> = arrayOf(
        ApiErrorCode.STRING_LENGTH_VIOLATION,
        ApiErrorCode.PATTERN_VIOLATION
    )
}
```

#### Spring Bean Validators

Validators can be Spring beans to access services or repositories:

```kotlin
@Component
class UniqueEmailValidator(
    private val userRepository: UserRepository
) : ConstraintValidator<CharSequence, UniqueEmailConstraint>() {

    override suspend fun validate(
        value: CharSequence?,
        constraint: UniqueEmailConstraint,
        context: ValidationContext
    ): ApiError? {
        value ?: return null
        if (userRepository.existsByEmail(value.toString())) {
            return ApiError(
                code = CustomErrorCode.EMAIL_TAKEN,
                message = "Email is already in use"
            )
        }
        return null
    }

    override fun applicableErrorCodes() = arrayOf(CustomErrorCode.EMAIL_TAKEN)
}
```

> **Key rules for custom constraints:**
> - Constraints are auto-discovered — no manual registration needed
> - Validators must be **stateless** (they are shared across requests)
> - Return `null` from `validate()` for null values (null handling is the caller's responsibility via `@Required`)
> - The `ApiError` returned from `validate()` only needs `code` — `path` and `location` are filled in automatically by the framework. `message` is optional.
> - Implement `applicableErrorCodes()` to declare which error codes your validator can produce
> - Metadata constructor parameter names must match annotation property names exactly

## Performance Considerations

### Precomputed Schemas

- **One-time analysis**: Field scanning and constraint resolution happen only once at startup
- **Cached schemas**: Validation schemas are built once and reused for all requests
- **No reflection at runtime**: Field access uses precomputed `FieldAccessor` instances

### Fast Field Access Strategy Ladder

`AccessorFactory` selects the fastest available strategy:

1. **Map lookup** — for header/query/param maps
2. **Public getter** — standard `getX()`/`isX()` methods
3. **VarHandle** — direct memory access (JVM 9+, near-native performance)
4. **MethodHandle** — fast reflection alternative via `unreflectGetter`
5. **Reflection** — `Field.get()` as last resort

### Validation Optimizations

- **Fail-fast mode**: `singleErrorPerField = true` stops at first error per field
- **Constraint ordering**: `@Required` constraints are always checked first
- **Zero-copy collections**: `CollectionUtils.normalizeList` avoids element copies when possible
- **Error deduplication**: Removes duplicate errors by `(path, code, location)` tuple
- **Group filtering**: Efficient set-intersection check skips non-matching constraints

## Why Use This Framework?

### Comparison with Standard Bean Validation

| Feature | This Framework | Bean Validation (JSR-380) |
|---------|---------------|--------------------------|
| **Runtime reflection** | None — precomputed accessors | Every validation call |
| **Schema generation** | Once at startup, cached | On each validation |
| **Error structure** | API-ready `ApiError` with paths, codes, locations | `ConstraintViolation` requires mapping |
| **Dynamic validation** | First-class `validate<T>()` API | Difficult to implement |
| **Error messages** | Optional per-constraint `message` property | Requires `MessageSource` config |
| **Validation groups** | First-class with `OnCreate`/`OnUpdate`/custom | Available but verbose |
| **Manual validation** | Fluent `ApiErrorCollector` API | Limited programmatic API |
| **Cross-field rules** | `@EqualTo`, `@GreaterThan`, conditional `@Required` | Requires class-level validator |
| **WebFlux support** | Native `Mono`/`Flux`/`Flow`/`suspend` | Limited reactive integration |
| **Collection validation** | `@Distinct`, `@ArraySize`, nested array paths | Basic `@Size`/`@NotEmpty` |
| **Custom constraints** | Simple: annotation + metadata + validator | Complex: annotation + validator + message keys |

### Ideal For

- **API backends** — structured error responses with codes, paths, and locations
- **High-throughput services** — zero-reflection runtime with cached schemas
- **Complex domain models** — cross-field validation, conditional requirements, nested objects
- **Reactive microservices** — native WebFlux and coroutine integration
- **Dynamic data processing** — runtime schema generation for arbitrary payloads

## License

MIT License

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
SOFTWARE.
