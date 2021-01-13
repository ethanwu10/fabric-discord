package dev.ethanwu.mc.fabricdiscord.logic

import dev.ethanwu.mc.fabricdiscord.config.ConfigLoader
import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import dev.ethanwu.mc.fabricdiscord.discord.DiscordBot
import dev.ethanwu.mc.fabricdiscord.minecraft.MinecraftChat
import dev.ethanwu.mc.fabricdiscord.minecraft.message.Message
import dev.ethanwu.mc.fabricdiscord.util.DisposableHolder
import dev.ethanwu.mc.fabricdiscord.util.Reloadable
import dev.ethanwu.mc.fabricdiscord.util.reactor.linkDataOnly
import org.apache.logging.log4j.LogManager
import reactor.core.Disposable
import reactor.core.Disposables
import reactor.core.publisher.Sinks
import java.util.function.Supplier

class Manager(
    private val configLoader: ConfigLoader,
    private val minecraftChatHolder: MinecraftChatHolder,
) : Disposable, Reloadable {
    companion object {
        val LOGGER = LogManager.getLogger()!!
    }

    private inner class Instance(
        private val config: ServerConfig,
        private val minecraftChatHolder: MinecraftChatHolder,
        private val discordBot: DiscordBot?,
    ) : Disposable {

        private val heldResources: Disposable.Composite = Disposables.composite()

        private val minecraftMessageSink: Sinks.Many<Message> = Sinks.many().multicast().onBackpressureBuffer()

        init {
            // TODO: add retry implementation?
            heldResources.add { minecraftMessageSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST) }

            // TODO: add retry implementation?
            heldResources.add(linkDataOnly(
                minecraftChatHolder.get().messageFlux,
                minecraftMessageSink,
                Sinks.EmitFailureHandler.FAIL_FAST
            ))

            if (discordBot != null) {
                heldResources.add(discordBot.sendOutgoingMessages(minecraftMessageSink.asFlux())
                    .doOnError { LOGGER.error("Error in outbound send", it) }
                    // FIXME: remove retry cap / add backoff
                    .retry(5)
                    .subscribe()
                )
                heldResources.add(discordBot.onChatMessage()
                    // TODO: move to server thread
                    .doOnError { LOGGER.error("Error in inbound send", it) }
                    // FIXME: remove retry cap / add backoff
                    .retry(5)
                    .subscribe {
                        try {
                            minecraftChatHolder.get().broadcastMessage(it)
                        } catch (e: Exception) {
                            LOGGER.error("Error in inbound send", it)
                        }
                    }
                )
                // FIXME: implement admin channels
            }
        }

        override fun dispose() {
            heldResources.dispose()
        }

        override fun isDisposed(): Boolean = heldResources.isDisposed
    }

    private fun createInstance(): Instance {
        val config = configLoader.loadOrCreate()
        minecraftChatHolder.loadOrReload(config)
        return Instance(
            config = config,
            minecraftChatHolder = minecraftChatHolder,
            discordBot = config.discordConfig?.let { DiscordBot(config = it) }
        )
    }

    private var instanceHolder = DisposableHolder(createInstance())

    override fun reload() {
        instanceHolder.update(createInstance())
    }

    override fun dispose() {
        instanceHolder.dispose()
    }

    override fun isDisposed(): Boolean = instanceHolder.isDisposed

    /**
     * Singleton owner of MinecraftChat object
     */
    interface MinecraftChatHolder : Supplier<MinecraftChat>, Disposable {
        /**
         * Create the [MinecraftChat] instance, or, if it has already been
         * created, reload it
         */
        fun loadOrReload(config: ServerConfig)

        /**
         * Get the current [MinecraftChatHolder] instance
         *
         * This method will not be called before [loadOrReload] is invoked
         * *at least once*. In essence, it has `lateinit` semantics.
         */
        override fun get(): MinecraftChat
    }
}
