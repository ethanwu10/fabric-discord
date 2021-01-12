package dev.ethanwu.mc.fabricdiscord.minecraft.message

import net.minecraft.text.TranslatableText
import java.util.*

class AnnouncementChatMessage internal constructor(original: TranslatableText, uuid: UUID?) :
    StandardFormatMinecraftTranslationKeyedChatMessage(original, uuid) {
    init {
        assert(original.key == "chat.type.announcement")
    }
}

