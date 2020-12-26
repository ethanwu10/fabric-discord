package dev.ethanwu.mc.fabricdiscord.texttransformer

import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.HtmlInline
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import com.vladsch.flexmark.ast.Text as FlexmarkText
import net.minecraft.text.Text as MinecraftText

class MarkdownToMinecraft {
    companion object {
        private val parserOpts = MutableDataSet()
            // Not supported by Discord
            .set(Parser.HEADING_PARSER, false)
            .set(Parser.HTML_BLOCK_PARSER, false)
            .set(Parser.LIST_BLOCK_PARSER, false)

            // Keep block quotes as-is when rendering
            .set(Parser.BLOCK_QUOTE_PARSER, false)

            .set(
                Parser.EXTENSIONS, listOf(
                    StrikethroughExtension.create()
                )
            )

            .toImmutable()

        private val parser = Parser.builder(parserOpts).build()

        fun render(markdown: String): MinecraftText {
            val ast = parser.parse(markdown)
            return renderDocument(ast)
        }

        private fun formatAst(node: Node): String {
            return "${node.nodeName} ${node.children.map { formatAst(it) }}"
        }

        private fun renderDocument(node: Node): MinecraftText {
            val root = LiteralText("")
            renderNode(node, Style.EMPTY, root)
            return root
        }

        private fun renderNode(node: Node, style: Style, appender: MutableText) {
            val newStyle = renderNodeStyle(node, style)
            if (node is FlexmarkText ||
                // Discord doesn't support inline HTML, so render it literally
                node is HtmlInline
            ) {
                assert(!node.hasChildren())
                val t = LiteralText(node.chars.unescape())
                t.style = newStyle
                appender.append(t)
            } else {
                node.children.forEach {
                    renderNode(it, newStyle, appender)
                }
            }
        }

        private fun renderNodeStyle(node: Node, style: Style): Style {
            return when (node) {
                is Emphasis -> style.withItalic(true)
                is StrongEmphasis -> style.withBold(true)
                is Code -> style.withColor(TextColor.fromRgb(0xcccccc))
                is Strikethrough -> style.withFormatting(Formatting.STRIKETHROUGH)
                else -> style
            }
        }
    }
}
