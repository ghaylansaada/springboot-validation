package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass

/**
 * Abstract class representing metadata for a validation constraint.
 *
 * Extending of this class encapsulate the immutable configuration
 * data extracted from a specific constraint annotation.
 *
 * This unified class enables the validation framework to treat
 * different constraint metadata types generically.
 *
 * @property groups
 *   The set of validation groups this constraint belongs to.
 *   Constraints without groups are always active; otherwise,
 *   they are validated conditionally based on the active group(s).
 */
abstract class ConstraintMetadata
{
    abstract val groups: Set<KClass<*>>

    abstract val messages : Set<MessageMetadata>

    val appliesToContainer : Boolean = false
}