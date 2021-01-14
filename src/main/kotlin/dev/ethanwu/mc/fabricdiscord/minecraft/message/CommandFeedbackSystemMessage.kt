package dev.ethanwu.mc.fabricdiscord.minecraft.message

import dev.ethanwu.mc.fabricdiscord.minecraft.util.coerceTextLikeToString
import net.minecraft.text.TranslatableText

class CommandFeedbackSystemMessage(text: TranslatableText) : SystemMessage(text), MessageWithSource {
    override val source: SourceInfo = displayNameToSourceInfo(text.args[0])

    /**
     * Contents of the command feedback
     */
    val contents: String = coerceTextLikeToString(text.args[1])
}
