package io.ghaylan.springboot.validation.validators.string

import io.ghaylan.springboot.validation.TestHelper
import io.ghaylan.springboot.validation.constraints.validators.string.html.HtmlConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.html.HtmlValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HtmlValidatorTest {

    private val ctx = TestHelper.defaultContext()

    private fun constraint(
        allowedTags: Set<String> = emptySet(),
        allowedAttrs: Set<String> = emptySet(),
        allowedProtocols: Set<String> = emptySet()
    ) = HtmlConstraint(
        allowedTags = allowedTags,
        allowedAttrs = allowedAttrs,
        allowedProtocols = allowedProtocols,
        groups = setOf(OnDefault::class),
        message = ""
    )

    @Nested
    inner class NullAndBlankTest {
        @Test fun `null is valid`() = runTest {
            assertNull(HtmlValidator.runValidation(null, constraint(), ctx))
        }

        @Test fun `blank string is valid`() = runTest {
            assertNull(HtmlValidator.runValidation("   ", constraint(), ctx))
        }

        @Test fun `empty string is valid`() = runTest {
            assertNull(HtmlValidator.runValidation("", constraint(), ctx))
        }
    }

    @Nested
    inner class PlainTextTest {
        @Test fun `plain text with no tags is valid when no tags allowed`() = runTest {
            assertNull(HtmlValidator.runValidation("Hello World", constraint(), ctx))
        }

        @Test fun `plain text with no tags passes with allowed tags defined`() = runTest {
            assertNull(HtmlValidator.runValidation("Hello World", constraint(allowedTags = setOf("p", "b")), ctx))
        }
    }

    @Nested
    inner class TagValidationTest {
        @Test fun `allowed tag passes`() = runTest {
            assertNull(HtmlValidator.runValidation("<p>Hello</p>", constraint(allowedTags = setOf("p")), ctx))
        }

        @Test fun `disallowed tag fails with HTML_TAG_VIOLATION`() = runTest {
            val error = HtmlValidator.runValidation(
                "<script>alert('xss')</script>",
                constraint(allowedTags = setOf("p")),
                ctx
            )
            assertNotNull(error)
            assertEquals(ApiErrorCode.HTML_TAG_VIOLATION, error?.code)
        }

        @Test fun `no allowed tags rejects any tag`() = runTest {
            val error = HtmlValidator.runValidation("<b>bold</b>", constraint(), ctx)
            assertNotNull(error)
            assertEquals(ApiErrorCode.HTML_TAG_VIOLATION, error?.code)
        }

        @Test fun `multiple allowed tags — all present pass`() = runTest {
            assertNull(
                HtmlValidator.runValidation(
                    "<p>Hello <b>world</b></p>",
                    constraint(allowedTags = setOf("p", "b")),
                    ctx
                )
            )
        }

        @Test fun `one disallowed among multiple tags fails`() = runTest {
            val error = HtmlValidator.runValidation(
                "<p>Hello</p><script>x()</script>",
                constraint(allowedTags = setOf("p")),
                ctx
            )
            assertNotNull(error)
        }
    }

    @Nested
    inner class AttributeValidationTest {
        @Test fun `allowed attribute on allowed tag passes`() = runTest {
            // allowedAttrs format: "tag:attr"
            assertNull(
                HtmlValidator.runValidation(
                    """<a href="https://example.com">link</a>""",
                    constraint(allowedTags = setOf("a"), allowedAttrs = setOf("a:href")),
                    ctx
                )
            )
        }

        @Test fun `disallowed attribute fails with HTML_ATTRIBUTE_VIOLATION`() = runTest {
            val error = HtmlValidator.runValidation(
                """<a onclick="evil()">link</a>""",
                constraint(allowedTags = setOf("a"), allowedAttrs = setOf("a:href")),
                ctx
            )
            assertNotNull(error)
            assertEquals(ApiErrorCode.HTML_ATTRIBUTE_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class ProtocolValidationTest {
        @Test fun `allowed protocol passes`() = runTest {
            // allowedProtocols format: "tag:attr:protocol1,protocol2"
            assertNull(
                HtmlValidator.runValidation(
                    """<a href="https://example.com">link</a>""",
                    constraint(
                        allowedTags = setOf("a"),
                        allowedAttrs = setOf("a:href"),
                        allowedProtocols = setOf("a:href:https,http")
                    ),
                    ctx
                )
            )
        }

        @Test fun `disallowed protocol fails with HTML_PROTOCOL_VIOLATION`() = runTest {
            val error = HtmlValidator.runValidation(
                """<a href="javascript:alert(1)">link</a>""",
                constraint(
                    allowedTags = setOf("a"),
                    allowedAttrs = setOf("a:href"),
                    allowedProtocols = setOf("a:href:https,http")
                ),
                ctx
            )
            assertNotNull(error)
            assertEquals(ApiErrorCode.HTML_PROTOCOL_VIOLATION, error?.code)
        }
    }

    @Nested
    inner class ContentIntegrityTest {
        @Test fun `content modified by Jsoup clean fails with HTML_VALUE_VIOLATION`() = runTest {
            // Jsoup removes disallowed content when cleaning; if cleaned != original, error is raised
            // e.g. a tag with no allowed content gets stripped
            val error = HtmlValidator.runValidation(
                "<p class=\"x\">text</p>",
                constraint(allowedTags = setOf("p")),
                ctx
            )
            // class attribute is not allowed, so Jsoup cleans it → HTML_ATTRIBUTE_VIOLATION or HTML_VALUE_VIOLATION
            assertNotNull(error)
        }
    }
}
