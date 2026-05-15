package io.ghaylan.springboot.validation.integration

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.schema.RequestInputSchema
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.schema.ValidationSchemaBuilder
import io.ghaylan.springboot.validation.schema.ValidatorBuilder
import io.ghaylan.springboot.validation.utils.ReflectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Builds and caches constraint validators and validation schemas at startup.
 *
 * On [ContextRefreshedEvent], discovers all [Constraint]-annotated annotations, instantiates
 * their validators, and pre-compiles schemas for every `@ValidateInput` endpoint.
 * Dynamic schemas (for arbitrary classes) are generated and cached on first access.
 */
open class ValidationRegistry: ApplicationListener<ContextRefreshedEvent> {
	
	private val validators = HashMap<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*, *>>>()

	/** Static schemas keyed by method identifier; populated once on startup. */
	val staticSchemas = mutableMapOf<String, RequestInputSchema>()

	private val dynamicSchemas = ConcurrentHashMap<Class<*>, Map<String, PropertySpec>>()

	override fun onApplicationEvent(event: ContextRefreshedEvent) {
		val appContext = event.applicationContext
		
		ValidatorBuilder.buildValidators(appContext)
			.forEach {
				validators[it.key] = it.value
			}
		
		ValidationSchemaBuilder.generateStaticSchemas(appContext = appContext, allValidators = validators)
			.forEach {
				staticSchemas[it.key] = it.value
			}
	}
	
	/** Returns the pre-built static schema for [id], or `null` if none was registered. */
	fun getSchemaByRequest(id: String): RequestInputSchema? {
		return staticSchemas[id]
	}
	
	/**
	 * Returns [TypeInfo] and a [PropertySpec] map for [clazz], generating and caching the schema on first call.
	 *
	 * @throws IllegalStateException if the class has no fields or no constraints.
	 */
	fun resolveSchemaByClass(
		clazz: Class<*>,
	): Pair<TypeInfo, Map<String, PropertySpec>> {
		val typeInfo = ReflectionUtils.infoFromClass(clazz)
		val type = typeInfo.resolveType.java
		
		return typeInfo to dynamicSchemas.computeIfAbsent(type) {
			val specs = ValidationSchemaBuilder.generateSchemaForType(rootClass = type, allValidators = validators)
				?: error("Could not create validation schema for type ${type.name}")
			
			require(specs.second.isNotEmpty()) {
				"Object specs must contain at least one field for type ${type.name}"
			}
			
			require(specs.second.any { it.value.constraints.isNotEmpty() }) {
				"Object specs must contain at least one constraint for type ${type.name}"
			}
			
			specs.second
		}
	}
}