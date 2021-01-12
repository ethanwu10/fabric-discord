package dev.ethanwu.mc.fabricdiscord.minecraft.message

import net.minecraft.text.Text

open class SystemMessage internal constructor(val text: Text) : Message {
    override val flattened: String by text::string
}
