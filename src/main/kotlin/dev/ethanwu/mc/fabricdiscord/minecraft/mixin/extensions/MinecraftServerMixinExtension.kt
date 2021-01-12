package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.extensions

import dev.ethanwu.mc.fabricdiscord.minecraft.ServerInstance

interface MinecraftServerMixinExtension {
    val fabricDiscordServerInstance: ServerInstance
}
