package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.impl

import net.minecraft.network.MessageType
import net.minecraft.server.PlayerManager
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*

fun PlayerManager.onBroadcastChatMessage(text: Text, messageType: MessageType, uuid: UUID, info: CallbackInfo) {
    // Call stack at invocation:
    // 0: PlayerManagerMixinImplKt.onBroadcastChatMessage
    // 1: PlayerManagerMixin.onBroadcastChatMessage
    // 2: net.minecraft.server.PlayerManager.broadcastChatMessage
    // 3: ??
    @Suppress("ThrowableNotThrown")
    val stackTrace = Exception().stackTrace
    server.boundInstance.onChatHook(text, messageType, uuid, info, stackTrace)
}
