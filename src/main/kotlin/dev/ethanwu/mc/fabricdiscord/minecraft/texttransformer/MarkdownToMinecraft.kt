package dev.ethanwu.mc.fabricdiscord.minecraft.texttransformer

import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import net.minecraft.text.*
import net.minecraft.util.Formatting
import com.vladsch.flexmark.ast.Text as FlexmarkText
import net.minecraft.text.Text as MinecraftText

class MarkdownToMinecraft(val config: ServerConfig.FormattingConfig) {
    private val parserOpts = MutableDataSet()
        // Not supported by Discord
        .set(Parser.HEADING_PARSER, false)
        .set(Parser.HTML_BLOCK_PARSER, false)
        .set(Parser.LIST_BLOCK_PARSER, false)

        // Keep block quotes as-is when rendering
        .set(Parser.BLOCK_QUOTE_PARSER, false)

        .set(
            Parser.EXTENSIONS, listOf(
                StrikethroughExtension.create(),
                // TODO: make auto-linking configurable / disable-able
                AutolinkExtension.create(),
            )
        )

        .toImmutable()

    private val parser = Parser.builder(parserOpts).build()

    companion object {
        private val validLinkRegex = Regex("^[a-zA-Z]+://[a-zA-Z0-9]")
    }

    fun render(markdown: String): List<MinecraftText> {
        val ast = parser.parse(markdown)
        return renderDocument(ast)
    }

    private fun formatAst(node: Node): String {
        return if (node is FlexmarkText) {
            "${node.nodeName} \"${node.chars.unescape()}\""
        } else {
            "${node.nodeName} ${node.children.map { formatAst(it) }}"
        }
    }

    private fun renderDocument(node: Node): List<MinecraftText> {
        val w = TextWriter()
        renderNode(node, Style.EMPTY, w)
        return w.lines
    }

    private fun renderPlainTextNode(node: Node, style: Style, appender: TextWriter) {
        appender.append(node.chars.unescape(), style)
    }

    private fun renderNode(node: Node, style: Style, appender: TextWriter) {
        val newStyle = renderNodeStyle(node, style)
        when (node) {
            is FlexmarkText,
                // Discord doesn't support inline HTML, so render it literally
            is HtmlInline,
                // Link is [] style links, which are not allowed in user messages,
                // so render literally
            is Link,
                // Mail links are not supported
            is MailLink,
            -> {
                renderPlainTextNode(node, newStyle, appender)
            }
            is AutoLink -> {
                val linkTarget = node.url.unescape()
                if (validLinkRegex.containsMatchIn(linkTarget)) {
                    renderPlainTextNode(
                        node,
                        newStyle.withFormatting(Formatting.UNDERLINE)
                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, linkTarget)),
                        appender
                    )
                } else {
                    // no protocol / malformed, Discord does not autolink - render as plain text
                    // TODO: consider performing this step as a flexmark postprocessor (convert to Text node)
                    // We wouldn't need to do most of this if AutolinkExtension exposed
                    // its LinkExtractor options - removing LinkType.WWW would solve
                    // creating links for domains without protocols, but we would still
                    // need to handle degenerate cases such as "http://" (protocol with
                    // no domain)
                    renderPlainTextNode(node, newStyle, appender)
                }
            }
            is SoftLineBreak -> {
                appender.appendLinebreak()
            }
            else -> {
                node.children.forEach {
                    renderNode(it, newStyle, appender)
                }
                if (node is Paragraph) {
                    // TODO: make configurable? extract from source?
                    appender.appendLinebreak(2)
                }
            }
        }
    }

    private fun renderNodeStyle(node: Node, style: Style): Style = when (node) {
        is Emphasis -> style.withItalic(true)
        is StrongEmphasis -> style.withBold(true)
        is Code -> style.withColor(TextColor.fromRgb(0xcccccc))
        is Strikethrough -> style.withFormatting(Formatting.STRIKETHROUGH)
        else -> style
    }

    private inner class TextWriter {
        val lines: MutableList<MutableText> = mutableListOf(LiteralText(""))
        private var pendingLinebreaks: Int = 0

        private val realAppendLinebreak = if (config.breakLines) {
            { lines.add(LiteralText("")) }
        } else {
            { lines.last().append(LiteralText("\n")) }
        }

        private fun appendOne(text: MinecraftText) {
            if (pendingLinebreaks > 0) {
                repeat(pendingLinebreaks) { realAppendLinebreak() }
                pendingLinebreaks = 0
            }
            lines.last().append(text)
        }

        fun append(text: String, style: Style) {
            assert(!text.contains('\n'))
            appendOne(LiteralText(text).apply { this.style = style })
        }

        fun appendLinebreak(numLines: Int = 1) {
            assert(numLines >= 0)
            assert(pendingLinebreaks == 0)
            pendingLinebreaks = numLines
        }
    }
}
