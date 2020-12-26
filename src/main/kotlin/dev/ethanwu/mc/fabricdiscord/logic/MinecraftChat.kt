package dev.ethanwu.mc.fabricdiscord.logic

import dev.ethanwu.mc.fabricdiscord.texttransformer.MarkdownToMinecraft
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*

class MinecraftChat {
    companion object {
        fun onMixinHook(
            server: MinecraftServer,
            text: Text,
            messageType: MessageType,
            uuid: UUID,
            cbInfo: CallbackInfo
        ) {
            // Call stack at invocation:
            // 0: self
            // 1: PlayerManagerMixin.onBroadcastChatMessage
            // 2: net.minecraft.server.PlayerManager.broadcastChatMessage
            // 3: ??

            @Suppress("ThrowableNotThrown")
            val stackTrace = Exception().stackTrace
            if (stackTrace[3].className == Companion::class.java.name) {
                // We sent this message, don't trigger the hook
                return
            }

            onMessage(server, text, messageType, uuid, cbInfo)
        }

        private fun onMessage(
            server: MinecraftServer,
            text: Text,
            messageType: MessageType,
            uuid: UUID,
            cbInfo: CallbackInfo
        ) {
            Core.onMinecraftChat(server, text, messageType, uuid)

            if (messageType == MessageType.CHAT && text is TranslatableText && text.key == "chat.type.text") {
                // This is a chat message, format it via markdown

                // cancel broadcasting of the raw message
                cbInfo.cancel()

                // see net.minecraft.server.network.ServerPlayNetworkHandler::onGameMessage
                val playerName = text.args[0] as Text
                val messageContentsRaw = text.args[1] as String

                val messageContentsRendered = MarkdownToMinecraft.render(messageContentsRaw)
                broadcastMessage(
                    server,
                    TranslatableText(text.key, playerName, messageContentsRendered),
                    MessageType.CHAT,
                    uuid
                )
            }
        }

        /**
         * Send a message originating from the chat bridge
         *
         * Messages sent through this method will not trigger onMessage
         */
        fun broadcastMessage(server: MinecraftServer, text: Text, messageType: MessageType, uuid: UUID) {
            server.playerManager.broadcastChatMessage(text, messageType, uuid)
        }
    }
}
