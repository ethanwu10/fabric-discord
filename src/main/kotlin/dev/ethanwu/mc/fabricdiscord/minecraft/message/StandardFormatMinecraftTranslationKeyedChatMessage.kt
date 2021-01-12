package dev.ethanwu.mc.fabricdiscord.minecraft.message

import dev.ethanwu.mc.fabricdiscord.minecraft.util.coerceTextLikeToString
import net.minecraft.text.TranslatableText
import java.util.*

/**
 * Chat message using [TranslatableText] where the first arg is the sender's
 * name and the second arg is the message contents
 */
abstract class StandardFormatMinecraftTranslationKeyedChatMessage internal constructor(
    original: TranslatableText,
    uuid: UUID?,
) :
    MinecraftTranslationKeyedChatMessage(original, uuid) {

    override val senderName: String
        get() = coerceTextLikeToString(original.args[0])
    override val contentsRaw: Any
        get() = original.args[1]
}
