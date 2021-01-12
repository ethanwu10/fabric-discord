package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.impl

import dev.ethanwu.mc.fabricdiscord.minecraft.commands.registerAllCommands
import net.minecraft.server.command.CommandManager
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

// TODO: consider using fabric api
@Suppress("UNUSED_PARAMETER")
fun CommandManager.onConstruct(
    registrationEnvironment: CommandManager.RegistrationEnvironment,
    callbackInfo: CallbackInfo,
) {
    registerAllCommands(dispatcher, registrationEnvironment)
}
