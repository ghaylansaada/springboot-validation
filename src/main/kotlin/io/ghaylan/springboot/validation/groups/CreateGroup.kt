package io.ghaylan.springboot.validation.groups

/**
 * Marker interface for the **Create** validation group.
 *
 * Use this group to associate constraints that should only apply when
 * **creating** a resource or entity.
 *
 * **Example:**
 * ```kotlin
 * @PostMapping("/users")
 * @ValidateInput(groups = [CreateGroup::class])
 * fun createUser(@RequestBody dto: UserDTO)
 * ```
 */
interface CreateGroup