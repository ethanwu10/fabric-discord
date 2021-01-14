package dev.ethanwu.mc.fabricdiscord.minecraft

import dev.ethanwu.mc.fabricdiscord.config.ConfigLoader
import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import dev.ethanwu.mc.fabricdiscord.logic.Manager
import dev.ethanwu.mc.fabricdiscord.minecraft.message.ChatMessage
import dev.ethanwu.mc.fabricdiscord.util.Reloadable
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.*

class ServerInstance(val server: MinecraftServer) : AutoCloseable, Disposable, Reloadable {
    companion object {
        val LOGGER = LogManager.getLogger()!!
    }

    private val serverThreadScheduler = Schedulers.fromExecutor(server)

    private val minecraftChatHolder = object : Manager.MinecraftChatHolder {
        lateinit var minecraftChat: MinecraftChat

        override fun loadOrReload(config: ServerConfig) {
            minecraftChat = MinecraftChat(server, config)
        }

        override fun get(): MinecraftChat = minecraftChat

        override fun subscribeToIncomingMessages(flux: Flux<ChatMessage>): Disposable =
            // Chat messages do not need to be on main thread (see
            // ServerPlayNetworkHandler#onGameMessage)
            flux.subscribe {
                try {
                    minecraftChat.broadcastMessage(it)
                } catch (e: Exception) {
                    LOGGER.error("Error in inbound send", it)
                }
            }

        override fun dispose() {
            // No-op since MinecraftChat is not Disposable
        }
    }

    private var manager = Manager(ConfigLoader(FabricLoader.getInstance()), minecraftChatHolder)

    override fun reload() {
        manager.reload()
    }

    override fun close() {
        manager.dispose()
        minecraftChatHolder.dispose()
    }

    override fun dispose() = close()

    // TODO consider checking minecraftChatHolder as well
    override fun isDisposed(): Boolean = manager.isDisposed

    fun onChatHook(
        text: Text,
        messageType: MessageType,
        uuid: UUID,
        info: CallbackInfo,
        stackTrace: Array<StackTraceElement>,
    ) = minecraftChatHolder.get().onMixinHook(text, messageType, uuid, info, stackTrace)
}
