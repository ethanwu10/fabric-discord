package dev.ethanwu.mc.fabricdiscord.minecraft.message

import dev.ethanwu.mc.fabricdiscord.minecraft.proxy.MessageType
import dev.ethanwu.mc.fabricdiscord.minecraft.util.nullable
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import java.util.*

fun convertToMessage(text: Text, type: MessageType?, uuid: UUID): Message =
    when (text) {
        is TranslatableText -> when {
            text.key.startsWith("chat.type.advancement") -> AdvancementSystemMessage(text)
            else -> null
        } ?: when (text.key) {
            "chat.type.text" -> PlayerTextChatMessage(text, uuid.nullable)
            "chat.type.emote" -> PlayerEmoteChatMessage(text, uuid.nullable)
            "chat.type.announcement" -> AnnouncementChatMessage(text, uuid.nullable)
            "chat.type.admin" -> CommandFeedbackSystemMessage(text)
            else -> null
        }
        else -> null
    } ?: when (type) {
        MessageType.SYSTEM -> SystemMessage(text)
        MessageType.CHAT, null -> UnknownMessage(text)
    }
