package io.ghaylan.springboot.validation.constraints

import kotlin.reflect.KClass

/**
 * Marks an annotation as a **validation constraint**, linking it to its corresponding metadata and validator classes.
 *
 * This meta-annotation is applied to constraint annotations (e.g., `@ValueIn`, `@Required`) to define:
 * - The [ConstraintMetadata] implementation that encapsulates the constraint's immutable configuration,
 *   including the validation groups it belongs to.
 * - One or more [ConstraintValidator] implementations responsible for enforcing the constraint logic at runtime.
 *
 * ### Purpose:
 * Annotating a validation annotation with `@Constraint` enables the validation framework to:
 * 1. Discover the associated metadata class that converts annotation instances into unified metadata objects
 *    implementing [ConstraintMetadata], which provides the `groups` property for group-based validation control.
 * 2. Identify validator classes that perform the actual validation logic using the metadata.
 * 3. Determine whether the constraint can be applied to **container types** (arrays, collections, or other structured types),
 *    not just single or simple values.
 *
 * ### Single/Container Types:
 * - **Single or simple types**: types without nested fields, including:
 *   - Primitive types (Int, Long, Boolean, etc.)
 *   - Strings, Dates
 *   - Arrays or collections of simple types (e.g., Array<String>, List<Long>)
 * - **Container types**: arrays, collections, or objects that may represent a group of elements
 *   or more complex structures. Constraints with `appliesToContainer = true` can be applied to these types.
 *
 * ### Usage Example:
 * ```kotlin
 * @Constraint(
 *   metadata = ValueInConstraintMetadata::class,
 *   validatedBy = [ValueInValidator::class],
 *   appliesToContainer = false // applies only to single/simple values
 * )
 * @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
 * @Retention(AnnotationRetention.RUNTIME)
 * annotation class ValueIn(
 *   val values: Array<String>,
 *   val groups: Array<KClass<*>> = [DefaultGroup::class]
 * )
 * ```
 *
 * ### Parameters:
 * @property metadata
 *   The [ConstraintMetadata] subclass representing the constraint's configuration.
 *   Must implement the `groups` property for group-based validation.
 *
 * @property validatedBy
 *   An array of [ConstraintValidator] classes responsible for enforcing the constraint logic.
 *   Validators implement `ConstraintValidator<T, M>` where `T` is the validated value type and
 *   `M` is the metadata type.
 *
 * @property appliesToContainer
 *   Indicates whether this constraint can be applied to **container types** (arrays, collections, or structured objects).
 *   - `true`: The constraint can validate the container itself.
 *   - `false` (default): The constraint is intended only for single/simple values.
 *
 * ### Retention and Target:
 * - Retained at runtime to enable reflective discovery and processing by the validation framework.
 * - Targeted at annotation classes to designate them as validation constraints.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Constraint(
    val metadata : KClass<out ConstraintMetadata>,
    val validatedBy: Array<KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>>,
    val appliesToContainer : Boolean = true)