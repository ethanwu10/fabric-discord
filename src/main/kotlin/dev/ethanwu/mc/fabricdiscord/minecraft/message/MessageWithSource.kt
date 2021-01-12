package dev.ethanwu.mc.fabricdiscord.minecraft.message

interface MessageWithSource : Message {
    /**
     * The sender of the message
     */
    val source: SourceInfo
}
