package dev.ethanwu.mc.fabricdiscord.minecraft.message

interface ChatMessage : MessageWithSource {
    /**
     * The contents of the message, in Markdown format
     */
    val contents: String
}
