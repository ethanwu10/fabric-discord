package dev.ethanwu.mc.fabricdiscord.minecraft.commands

import dev.ethanwu.mc.fabricdiscord.FabricDiscord
import dev.ethanwu.mc.fabricdiscord.minecraft.mixin.impl.boundInstance
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.text.LiteralText

val reloadCommand: Command = literal("reload")
    .requires { it.hasPermissionLevel(4) }
    .executes { c ->
        try {
            c.source.minecraftServer.boundInstance.reload()
            c.source.sendFeedback(LiteralText("Discord bridge config reloaded"), true)
            0
        } catch (e: Exception) {
            FabricDiscord.LOGGER.error("Config reload failed", e)
            c.source.sendFeedback(LiteralText("Discord bridge config reload failed!"), true)
            c.source.sendFeedback(LiteralText("${e::class.simpleName}: ${e.message}"), false)
            -1
        }
    }
