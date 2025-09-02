package io.ghaylan.springboot.validation.constraints.validators.string.html

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import org.jsoup.safety.Safelist


object HtmlValidator : ConstraintValidator<CharSequence, HtmlConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: HtmlConstraint,
        context: ValidationContext
    ): ApiError?
    {
        if (value.isNullOrBlank()) return null

        val attrsPerTag = parseAllowedAttributes(constraint.allowedAttrs)
        val protocolsPerTagAttr = parseAllowedProtocols(constraint.allowedProtocols)

        val safelist = buildDynamicSafelist(
            allowedTags = constraint.allowedTags.toList(),
            allowedAttributes = attrsPerTag,
            allowedProtocols = protocolsPerTagAttr)

        val doc = org.jsoup.Jsoup.parseBodyFragment(value.toString())

        checkTagErrors(doc, constraint.allowedTags)?.let { return it }
        checkAttributeErrors(doc, attrsPerTag)?.let { return it }
        checkProtocolErrors(doc, protocolsPerTagAttr)?.let { return it }

        val cleaned = org.jsoup.Jsoup.clean(value.toString(), safelist)

        if (cleaned != value.toString()) {
            return ApiError(code = ApiErrorCode.HTML_VALUE_VIOLATION, message = "HTML content contains disallowed content")
        }

        return null
    }

    private fun buildDynamicSafelist(
        allowedTags: List<String>,
        allowedAttributes: Map<String, List<String>>,
        allowedProtocols: Map<String, Map<String, List<String>>>
    ) : Safelist
    {
        val safelist = Safelist.none()

        // Add allowed tags
        allowedTags.forEach { safelist.addTags(it) }

        // Add allowed attributes per tag
        allowedAttributes.forEach { (tag, attrs) ->
            safelist.addAttributes(tag, *attrs.toTypedArray())
        }

        // Add allowed protocols per tag and attribute
        allowedProtocols.forEach { (tag, attrProtocols) ->
            attrProtocols.forEach { (attr, protocols) ->
                safelist.addProtocols(tag, attr, *protocols.toTypedArray())
            }
        }

        return safelist
    }


    private fun parseAllowedAttributes(
        allowedAttrs: Set<String>
    ): Map<String, List<String>>
    {
        return allowedAttrs
            .mapNotNull {
                val parts = it.split(':', limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .groupBy({ it.first }, { it.second })
    }


    private fun parseAllowedProtocols(
        allowedProtocols: Set<String>
    ): Map<String, Map<String, List<String>>>
    {
        // Example: parse strings like "a:href:http,https"
        val map = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

        allowedProtocols.forEach { entry ->
            // Split to tag:attr:protocols (comma separated)
            val parts = entry.split(':', limit = 3)
            if (parts.size == 3) {
                val (tag, attr, protocolsStr) = parts
                val protocols = protocolsStr.split(',').map { it.lowercase() }

                val attrMap = map.getOrPut(tag) { mutableMapOf() }
                attrMap[attr] = protocols.toMutableList()
            }
        }

        return map
    }


    private fun checkTagErrors(
        doc: org.jsoup.nodes.Document,
        allowedTags: Set<String>
    ): ApiError?
    {
        doc.body().allElements.forEach { element ->
            val tag = element.tagName()
            if (tag != "#root" && tag != "body" && !allowedTags.contains(tag)) {
                return ApiError(code = ApiErrorCode.HTML_TAG_VIOLATION, message = "HTML tag <$tag> is not allowed")
            }
        }
        return null
    }


    private fun checkAttributeErrors(
        doc: org.jsoup.nodes.Document,
        allowedAttrs: Map<String, List<String>>
    ): ApiError?
    {
        doc.body().allElements.forEach { element ->
            val tag = element.tagName()
            val allowed = allowedAttrs[tag] ?: emptyList()
            element.attributes().forEach { attr ->
                if (!allowed.contains(attr.key)) {
                    return ApiError(code = ApiErrorCode.HTML_ATTRIBUTE_VIOLATION, message = "HTML attribute '${attr.key}' is not allowed on <$tag>")
                }
            }
        }
        return null
    }


    private fun checkProtocolErrors(
        doc: org.jsoup.nodes.Document,
        allowedProtocols: Map<String, Map<String, List<String>>>
    ): ApiError?
    {
        doc.body().allElements.forEach { element ->
            val tag = element.tagName()
            val attrProtocols = allowedProtocols[tag] ?: emptyMap()

            element.attributes().forEach { attr ->
                val allowedProtocols = attrProtocols[attr.key] ?: emptyList()
                if (allowedProtocols.isNotEmpty()) {
                    val value = attr.value
                    val protocol = value.substringBefore(":", "").lowercase()
                    if (protocol.isNotEmpty() && !allowedProtocols.contains(protocol)) {
                        return ApiError(code = ApiErrorCode.HTML_PROTOCOL_VIOLATION, message = "HTML protocol '$protocol' is not allowed in attribute '${attr.key}' on <$tag>")
                    }
                }
            }
        }
        return null
    }
}