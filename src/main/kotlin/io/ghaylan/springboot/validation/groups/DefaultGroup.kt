package io.ghaylan.springboot.validation.groups

/**
 * Marker interface for the **Default** validation group.
 *
 * Every constraint is implicitly part of this group unless explicitly
 * assigned to a different group. This group is used when no `groups`
 * parameter is provided during validation.
 *
 * **Example:**
 * ```kotlin
 * engine.validate<UserDTO>(user) // Uses DefaultGroup
 * ```
 */
interface DefaultGroup