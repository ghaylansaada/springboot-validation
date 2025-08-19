package io.ghaylan.springboot.validation.groups

/**
 * Marker interface for the **Update** validation group.
 *
 * Use this group to associate constraints that should only apply when
 * **updating** a resource or entity.
 *
 * **Example:**
 * ```kotlin
 * @PutMapping("/users/{id}")
 * @ValidateInput(groups = [UpdateGroup::class])
 * fun updateUser(@RequestBody dto: UserDTO)
 * ```
 */
interface UpdateGroup