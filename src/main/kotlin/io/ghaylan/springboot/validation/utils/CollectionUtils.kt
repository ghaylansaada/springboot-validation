package io.ghaylan.springboot.validation.utils

/**
 * Normalizes arrays and collections into a null-free, read-only `List<Any>`.
 *
 * Uses zero-copy wrappers when possible; allocates only when nulls must be filtered
 * or a non-list Collection must be copied for stable index order.
 */
object CollectionUtils {
	
	/**
	 * Normalize an input into a non-null, indexable List<Any> without copying
	 * when possible. Returns read-only views for Arrays and Lists when they
	 * contain no nulls.
	 *
	 * - null                  -> emptyList()
	 * - Array<T?> (no nulls)  -> zero-copy view (array-backed), read-only
	 * - Array<T?> (has nulls) -> allocates ArrayList without nulls
	 * - List<*> (no nulls)    -> zero-copy view (list-backed), read-only
	 * - List<*> (has nulls)   -> allocates ArrayList without nulls
	 * - Other Collection<*>   -> copies into ArrayList to preserve stable index order
	 * - Primitive arrays      -> Kotlin's asList() returns a boxed, read-only view (no element copy)
	 */
	fun normalizeList(value: Any?): List<Any> {
		if (value == null) return emptyList()
		
		return when (value) {
			is Array<*> -> normalizeArray(value)
			is List<*> -> normalizeListValue(value)
			is Collection<*> -> normalizeCollection(value)
			is BooleanArray -> value.asList()
			is ByteArray -> value.asList()
			is ShortArray -> value.asList()
			is IntArray -> value.asList()
			is LongArray -> value.asList()
			is FloatArray -> value.asList()
			is DoubleArray -> value.asList()
			else -> emptyList()
		}
	}
	
	private fun normalizeArray(array: Array<*>): List<Any> {
		return if (!arrayHasNulls(array)) arrayReadOnlyView(array)
		else array.filterNotNull()
	}
	
	private fun normalizeListValue(list: List<*>): List<Any> {
		return when {
			list.isEmpty() -> emptyList()
			!listHasNulls(list) -> listReadOnlyView(list)
			else -> list.filterNotNull()
		}
	}
	
	private fun normalizeCollection(collection: Collection<*>): List<Any> {
		return if (collection.isEmpty()) emptyList()
		else collection.filterNotNull()
	}
	
	/** Zero-allocation null check for reference arrays. */
	private fun arrayHasNulls(array: Array<*>): Boolean {
		for (e in array) if (e == null) return true
		return false
	}
	
	/** Zero-allocation null check for lists. */
	private fun listHasNulls(list: List<*>): Boolean {
		for (i in list.indices) if (list[i] == null) return true
		return false
	}
	
	/**
	 * Read-only, zero-copy view over a non-null element array.
	 * Caller must ensure array contains no nulls.
	 *
	 * - No element copies
	 * - No unsafe cast of the array itself
	 * - Indexable and fast
	 */
	private fun arrayReadOnlyView(array: Array<*>): List<Any> {
		return object: AbstractList<Any>() {
			override val size: Int = array.size
			override fun get(index: Int): Any = array[index]!!  // safe due to "no nulls" precheck
		}
	}
	
	/**
	 * Read-only, zero-copy view over a non-null element list.
	 * Caller must ensure list contains no nulls.
	 *
	 * - No element copies
	 * - Prevents accidental writes regardless of the original list's mutability
	 */
	private fun listReadOnlyView(list: List<*>): List<Any> {
		return object: AbstractList<Any>() {
			override val size: Int = list.size
			override fun get(index: Int): Any = list[index]!!   // safe due to "no nulls" precheck
		}
	}
}