package dev.ethanwu.mc.fabricdiscord.minecraft.proxy

import net.minecraft.network.MessageType as MinecraftMessageType

/**
 * Message types handled by the bridge
 *
 * Mirrors [net.minecraft.network.MessageType], created to remove direct ties to
 * mapped types
 */
enum class MessageType {
    CHAT, SYSTEM;

    val minecraft: MinecraftMessageType
        get() = when (this) {
            CHAT -> MinecraftMessageType.CHAT
            SYSTEM -> MinecraftMessageType.SYSTEM
        }

    companion object {
        fun fromMinecraft(minecraft: MinecraftMessageType): MessageType? = when (minecraft) {
            MinecraftMessageType.CHAT -> CHAT
            MinecraftMessageType.SYSTEM -> SYSTEM
            else -> null
        }
    }
}

val MinecraftMessageType.wrapped: MessageType?
    get() = MessageType.fromMinecraft(this)
