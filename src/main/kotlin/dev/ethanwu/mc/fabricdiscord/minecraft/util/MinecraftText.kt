package dev.ethanwu.mc.fabricdiscord.minecraft.util

import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

/**
 * Convert an [Any] containing [Text] or [String] to a [String]
 *
 * Intended for use on elements of [TranslatableText.args]
 */
fun coerceTextLikeToString(text: Any): String = when (text) {
    is String -> text
    is Text -> text.string
    else -> throw IllegalArgumentException("Invalid type provided (not Text or String)")
}
