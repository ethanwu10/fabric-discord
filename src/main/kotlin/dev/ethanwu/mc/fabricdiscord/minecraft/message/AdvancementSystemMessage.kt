package dev.ethanwu.mc.fabricdiscord.minecraft.message

import net.minecraft.text.TranslatableText

class AdvancementSystemMessage(text: TranslatableText) : SystemMessage(text), MessageWithSource {
    override val source: SourceInfo = displayNameToSourceInfo(text.args[0])

    init {
        assert(text.key.startsWith("chat.type.advancement"))
    }
}
