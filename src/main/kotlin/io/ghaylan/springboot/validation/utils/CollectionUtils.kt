package io.ghaylan.springboot.validation.utils

/**
 * A utility object providing optimized, read‑only views and normalization helpers
 * for arrays and collections. Its primary purpose is to **normalize arbitrary inputs**
 * into a canonical `List<Any>` representation without unnecessary allocations.
 *
 * ---
 * ### Responsibilities
 *
 * - Provide a unified, **null‑free**, indexable `List<Any>` view over different input types:
 *   * Kotlin/JVM reference arrays (`Array<T?>`)
 *   * Primitive arrays (`IntArray`, `ByteArray`, etc.)
 *   * Standard `List<*>` implementations
 *   * Other `Collection<*>` types (e.g., `Set`, `Queue`)
 * - Avoid **element copies** and **extra allocations** whenever possible:
 *   * Uses **zero‑copy** wrappers for arrays and lists when no `null` elements are present.
 *   * Allocates a new `ArrayList` **only** when input contains `null`s or is a non‑list `Collection`.
 * - Expose **read‑only** views to prevent accidental mutation of underlying data.
 *
 * ---
 * ### Null‑handling
 *
 * - Any `null` values are **filtered out** from the resulting list.
 * - For empty inputs, the function returns `emptyList()`.
 * - If the entire array/collection is `null`, the function also returns `emptyList()`.
 *
 * ---
 * ### Example Usage
 *
 * ```kotlin
 * // Reference array without nulls -> zero-copy, read-only view
 * val arr = arrayOf("a", "b", "c")
 * val list = CollectionUtils.normalizeList(arr)
 * println(list) // [a, b, c]
 *
 * // List with nulls -> new ArrayList without nulls
 * val input = listOf("x", null, "y")
 * val result = CollectionUtils.normalizeList(input)
 * println(result) // [x, y]
 *
 * // Primitive array -> boxed, read-only view
 * val ints = intArrayOf(1, 2, 3)
 * val boxed = CollectionUtils.normalizeList(ints)
 * println(boxed) // [1, 2, 3]
 * ```
 *
 * ---
 * ### Design Notes
 *
 * - The returned lists from zero‑copy paths are **read‑only wrappers** using
 *   `AbstractList`, ensuring no mutation of the underlying arrays or lists.
 * - Non‑`List` collections (e.g., `Set`) are copied into an `ArrayList` to preserve
 *   **deterministic index order**.
 * - Performance has been tuned for large inputs:
 *   * Null checks are **O(n)** but zero‑allocation.
 *   * Primitive arrays leverage Kotlin's native `asList()`, which is a boxing, read‑only view.
 *
 * ---
 * ### Complexity
 *
 * - **Time**: O(n) worst‑case (for null filtering); O(1) when returning a view directly.
 * - **Space**: O(1) when returning a view; O(n) only when filtering or copying is required.
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