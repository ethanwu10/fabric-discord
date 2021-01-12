package dev.ethanwu.mc.fabricdiscord.minecraft

import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import dev.ethanwu.mc.fabricdiscord.minecraft.message.ChatMessage
import dev.ethanwu.mc.fabricdiscord.minecraft.message.Message
import dev.ethanwu.mc.fabricdiscord.minecraft.message.convertToMessage
import dev.ethanwu.mc.fabricdiscord.minecraft.proxy.MessageType
import dev.ethanwu.mc.fabricdiscord.minecraft.proxy.NIL_UUID
import dev.ethanwu.mc.fabricdiscord.minecraft.proxy.wrapped
import dev.ethanwu.mc.fabricdiscord.minecraft.texttransformer.MarkdownToMinecraft
import net.minecraft.server.MinecraftServer
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.*

class MinecraftChat(
    private val server: MinecraftServer,
    private val config: ServerConfig,
) {
    private val formatter: MarkdownToMinecraft? = config.formattingConfig?.let { MarkdownToMinecraft(it) }

    // The hook is called on the (single) main server thread, so `unsafe` is safe
    private val sink: Sinks.Many<Message> = Sinks.unsafe().many().multicast().onBackpressureBuffer()
    val messageFlux: Flux<Message> get() = sink.asFlux()

    internal fun onMixinHook(
        text: Text,
        messageType: net.minecraft.network.MessageType,
        uuid: UUID,
        cbInfo: CallbackInfo,
        stackTrace: Array<StackTraceElement>,
    ) {
        // See PlayerManagerMixinImplKt.onBroadcastChatMessage

        if (stackTrace[3].className == MinecraftChat::class.java.name) {
            // We sent this message, don't trigger the hook
            return
        }

        onMessage(text, messageType.wrapped, uuid, cbInfo)
    }

    private fun MarkdownToMinecraft.renderAndBroadcast(
        markdown: String,
        messageType: MessageType,
        uuid: UUID?,
        textFactory: (Text) -> Text,
    ) {
        render(markdown).forEach { broadcastMessage(textFactory(it), messageType, uuid) }
    }

    private fun onMessage(
        text: Text,
        messageType: MessageType?,
        uuid: UUID,
        cbInfo: CallbackInfo,
    ) {
        sink.emitNext(convertToMessage(text, messageType, uuid), Sinks.EmitFailureHandler.FAIL_FAST)

        if (formatter != null &&
            messageType == MessageType.CHAT && text is TranslatableText &&
            (text.key == "chat.type.text" || text.key == "chat.type.emote")
        ) {
            // This is a chat message, format it via markdown

            // cancel broadcasting of the raw message
            cbInfo.cancel()

            // see net.minecraft.server.network.ServerPlayNetworkHandler::onGameMessage
            val playerName = text.args[0] as Text
            val messageContentsRaw = text.args[1] as String

            formatter.renderAndBroadcast(messageContentsRaw, MessageType.CHAT, uuid) {
                TranslatableText(text.key, playerName, it)
            }
        }
    }

    /**
     * Send a message originating from the chat bridge
     *
     * Messages sent through this method will not trigger onMessage
     */
    fun broadcastMessage(text: Text, messageType: MessageType, uuid: UUID?) {
        server.playerManager.broadcastChatMessage(text, messageType.minecraft, uuid ?: NIL_UUID)
    }

    fun broadcastMessage(message: ChatMessage) {
        // TODO: style appropriately (most likely extend source info)
        val renderedSource: Text = LiteralText(message.source.name).styled {
            it.withInsertion(message.source.name)
        }

        // TODO: make formatting visually distinct / configurable
        val textFactory: (Text) -> Text = { TranslatableText("chat.type.text", renderedSource, it) }

        if (formatter != null) {
            formatter.renderAndBroadcast(message.contents, MessageType.CHAT, message.source.uuid, textFactory)
        } else {
            broadcastMessage(textFactory(LiteralText(message.contents)), MessageType.CHAT, message.source.uuid)
        }
    }
}
