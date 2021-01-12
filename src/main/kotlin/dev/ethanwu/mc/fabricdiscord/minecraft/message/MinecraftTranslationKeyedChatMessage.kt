package dev.ethanwu.mc.fabricdiscord.minecraft.message

import dev.ethanwu.mc.fabricdiscord.minecraft.util.coerceTextLikeToString
import net.minecraft.text.TranslatableText
import java.util.*

/**
 * Chat message whose components are parsed from a [TranslatableText]
 */
abstract class MinecraftTranslationKeyedChatMessage internal constructor(
    val original: TranslatableText,
    uuid: UUID?,
) : ChatMessage {
    /**
     * Internal function used to construct [source]
     */
    protected abstract val senderName: String

    /**
     * The raw message contents as extracted from [original]
     */
    abstract val contentsRaw: Any

    override val source by lazy { SourceInfo(senderName, uuid) }
    override val contents: String by lazy { coerceTextLikeToString(contentsRaw) }

    override val flattened: String by original::string
}
