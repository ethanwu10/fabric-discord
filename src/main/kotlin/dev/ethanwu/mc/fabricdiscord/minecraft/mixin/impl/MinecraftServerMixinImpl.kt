package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.impl

import dev.ethanwu.mc.fabricdiscord.minecraft.ServerInstance
import dev.ethanwu.mc.fabricdiscord.minecraft.mixin.extensions.MinecraftServerMixinExtension
import net.minecraft.server.MinecraftServer
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Suppress("UNUSED_PARAMETER")
fun MinecraftServer.onSetupServer(info: CallbackInfo) {
}

@Suppress("UNUSED_PARAMETER")
fun MinecraftServer.onShutdown(info: CallbackInfo) {
    // If the mod crashes at startup (in the creation of the bound instance),
    // then `boundInstance` will still be null when the server invokes the
    // shutdown handler as a result of the crash.
    @Suppress("UNNECESSARY_SAFE_CALL")
    this.boundInstance?.close()
}

val MinecraftServer.boundInstance: ServerInstance
    get() {
        @Suppress("CAST_NEVER_SUCCEEDS") val extensionInstance = this as MinecraftServerMixinExtension
        return extensionInstance.fabricDiscordServerInstance
    }
