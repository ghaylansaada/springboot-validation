package io.ghaylan.springboot.validation.accessor

import io.ghaylan.springboot.validation.ext.bodyFieldName
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

/**
 * Build a [PropertyAccessor] for this reflective [Field].
 *
 * ### Steps
 * 1. `trySetAccessible()` (best-effort; ignored if not permitted).
 * 2. Use `MethodHandles.lookup().unreflectGetter(this)` to obtain a JVM getter [java.lang.invoke.MethodHandle].
 * 3. Wrap in [PropertyAccessor].
 *
 * ### When It Can Fail
 * - `IllegalAccessException`: access denied (JPMS/module restrictions).
 * - Security restrictions preventing deep reflection.
 *
 * In production, call this from code that can fall back (e.g., to reflection or a getter).
 *
 * @throws IllegalAccessException if the lookup fails.
 */
@Throws(IllegalAccessException::class)
fun Field.buildFieldAccessor() : PropertyAccessor
{
    // Best effort to relax access checks (ignored if not permitted)
    this.trySetAccessible()

    // Standard (non-privateLookupIn) lookup; sufficient when field is public or module is open.
    val lookup = MethodHandles.lookup()

    // Convert the reflective field into a direct MethodHandle for fast access.
    val handle = lookup.unreflectGetter(this)

    return PropertyAccessor(
        realName = this.name,
        resolvedName = this.bodyFieldName(),
        handle = handle,
        type = this.type)
}