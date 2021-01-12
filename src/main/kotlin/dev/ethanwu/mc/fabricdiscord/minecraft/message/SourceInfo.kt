package dev.ethanwu.mc.fabricdiscord.minecraft.message

import dev.ethanwu.mc.fabricdiscord.minecraft.util.coerceTextLikeToString
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.util.*

data class SourceInfo(
    /** Display name of the sender */
    val name: String,
    /** UUID of the sender if they are a player (null otherwise) */
    val uuid: UUID?,
)

fun HoverEvent.EntityContent.toSourceInfo(): SourceInfo? = name?.let { name -> SourceInfo(name.string, uuid) }

/**
 * Convert a display name to a [SourceInfo] object
 *
 * @param text *should* originate from a [net.minecraft.entity.Entity.getDisplayName]
 *             call and thus be [Text], however [String] is also accepted.
 */
fun displayNameToSourceInfo(text: Any) =
    (text as? Text)?.style?.hoverEvent?.getValue(HoverEvent.Action.SHOW_ENTITY)?.toSourceInfo()
        ?: SourceInfo(coerceTextLikeToString(text), null)
