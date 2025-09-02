package io.ghaylan.springboot.validation.integration

import io.ghaylan.springboot.validation.groups.CreateGroup
import io.ghaylan.springboot.validation.groups.DefaultGroup
import io.ghaylan.springboot.validation.groups.UpdateGroup
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import kotlin.reflect.KClass

/**
 * Enables automatic validation of HTTP request inputs on a controller or handler method.
 *
 * When applied to a Spring MVC controller method (or any HTTP endpoint handler), this annotation
 * triggers validation of the incoming request data according to the specified configuration.
 *
 * ### Validation Targets:
 * - **Request Body:** Controlled by [validateBody], validates the deserialized body payload.
 * - **Query Parameters:** Controlled by [validateQuery], validates parameters in the URL query string.
 * - **Path Variables:** Controlled by [validatePath], validates variables extracted from the URI path.
 * - **Headers:** Controlled by [validateHeaders], validates HTTP request headers.
 *
 * ### Validation Flow:
 * - Validation schemas for each input section are resolved dynamically or statically via [ValidationRegistry].
 * - All applicable constraints on the inputs are evaluated recursively.
 * - Errors are collected as a list of [ApiError] instances.
 * - If any constraint violations occur, a [ConstraintViolationException] is thrown automatically.
 *
 * ### Validation Groups:
 * - The [groups] property specifies which validation groups to activate for this invocation.
 * - If not explicitly set, the [DefaultGroup] is used.
 * - By leveraging validation groups, it's possible to apply different rules depending on the use case.
 *   For example:
 *   - [CreateGroup]: Used during creation operations.
 *   - [UpdateGroup]: Used during update operations.
 *   - [DefaultGroup]: Used as the fallback group when no specific group is set.
 * - Developers can define additional custom validation groups by creating their own marker interfaces or classes.
 *
 * ### Example Usage:
 * ```kotlin
 * @PostMapping("/users")
 * @ValidateInput(groups = [CreateGroup::class])
 * fun createUser(@RequestBody dto: UserDTO) { ... }
 * ```
 *
 * @property validateBody Whether to validate the HTTP request body. Defaults to `true`.
 * @property validateQuery Whether to validate query parameters. Defaults to `true`.
 * @property validatePath Whether to validate path variables. Defaults to `true`.
 * @property validateHeaders Whether to validate request headers. Defaults to `true`.
 * @property singleErrorPerField If `true` (default), stops validation for a field after the first constraint failure,
 *                               reducing noise in error reports.
 * @property groups The set of validation groups to apply. Defaults to `[DefaultGroup]`.
 *                  Supported built-in groups: [DefaultGroup], [CreateGroup], [UpdateGroup].
 *                  You can also create your own group interfaces or classes.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateInput(
    val validateBody: Boolean = true,
    val validateQuery: Boolean = true,
    val validatePath: Boolean = true,
    val validateHeaders: Boolean = true,
    val singleErrorPerField : Boolean = true,
    val groups: Array<KClass<*>> = [DefaultGroup::class])