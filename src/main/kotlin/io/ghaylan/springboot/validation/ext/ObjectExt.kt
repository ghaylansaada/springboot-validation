package io.ghaylan.springboot.validation.ext

import java.util.*
import kotlin.collections.all
import kotlin.collections.isEmpty
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.sequences.all
import kotlin.sequences.none
import kotlin.text.isBlank
import kotlin.text.isWhitespace

/**
 * Checks if this object is considered null, void, or empty based on specific criteria.
 *
 * The function evaluates various types:
 * - `null` or [Unit] returns `true`.
 * - [Optional] returns `true` if not present.
 * - [Char] returns `true` if it represents an empty string.
 * - [CharSequence] returns `true` if blank.
 * - [Collection], [Sequence], [Array], or [Map] returns `true` if all elements/values are null or void.
 * - Enums return `false`.
 * - Other types return `false`.
 *
 * @return `true` if the object is null, void, or empty according to the defined criteria; `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun Any?.isDeepNullOrEmpty(
    visited : MutableSet<Any> = Collections.newSetFromMap(IdentityHashMap())
) : Boolean
{
    contract {
        returns(false) implies (this@isDeepNullOrEmpty != null)
    }

	if (this == null) return true
	if (this in visited) return false

	visited.add(this)
	
	return when (this)
	{
        is Unit -> true
        is Optional<*> -> !this.isPresent
        is Enum<*> -> false
        is Char -> this.isWhitespace() || this == '\u0000'
        is CharSequence -> this.isBlank()
        is Collection<*> -> this.isEmpty() || this.all { it.isDeepNullOrEmpty(visited) }
        is Sequence<*> -> this.none() || this.all { it.isDeepNullOrEmpty(visited) }
        is Array<*> -> this.isEmpty() || this.all { it.isDeepNullOrEmpty(visited) }
        is CharArray -> this.isEmpty()
        is ByteArray -> this.isEmpty()
        is ShortArray -> this.isEmpty()
        is IntArray -> this.isEmpty()
        is LongArray -> this.isEmpty()
        is FloatArray -> this.isEmpty()
        is DoubleArray -> this.isEmpty()
        is BooleanArray -> this.isEmpty()
        is Map<*,*> -> this.isEmpty() || this.values.all { it.isDeepNullOrEmpty(visited) }
        is Pair<*, *> -> this.first.isDeepNullOrEmpty(visited) && this.second.isDeepNullOrEmpty(visited)
        is Triple<*, *, *> -> this.first.isDeepNullOrEmpty(visited) && this.second.isDeepNullOrEmpty(visited) && this.third.isDeepNullOrEmpty(visited)
        else -> false
    }
}