package dev.ethanwu.mc.fabricdiscord.minecraft.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

internal typealias Command = ArgumentBuilder<ServerCommandSource, *>

fun registerAllCommands(
    dispatcher: CommandDispatcher<ServerCommandSource>,
    registrationEnvironment: CommandManager.RegistrationEnvironment,
) {
    val commandRoot = literal("discord")
    dispatcher.register(commandRoot
        .then(reloadCommand)
        // TODO: root status command?
    )
}

