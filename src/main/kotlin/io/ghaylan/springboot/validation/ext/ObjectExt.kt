package io.ghaylan.springboot.validation.ext

import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Returns `true` if the value is semantically absent: `null`, `Unit`, blank string, empty collection,
 * or a collection/map whose every element is itself deeply null or empty.
 *
 * Cycle detection via [visited] prevents infinite recursion on self-referential structures.
 * Enums and all other leaf types return `false` (they are considered present).
 */
@OptIn(ExperimentalContracts::class)
fun Any?.isDeepNullOrEmpty(
	visited: MutableSet<Any> = Collections.newSetFromMap(IdentityHashMap())
): Boolean {
	contract {
		returns(false) implies (this@isDeepNullOrEmpty != null)
	}
	
	if (this == null) return true
	if (this in visited) return false
	
	visited.add(this)
	
	return when (this) {
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
		is Map<*, *> -> this.isEmpty() || this.values.all { it.isDeepNullOrEmpty(visited) }
		is Pair<*, *> -> this.first.isDeepNullOrEmpty(visited) && this.second.isDeepNullOrEmpty(visited)
		is Triple<*, *, *> -> {
			this.first.isDeepNullOrEmpty(visited) && this.second.isDeepNullOrEmpty(visited) && this.third.isDeepNullOrEmpty(visited)
		}
		
		else -> false
	}
}