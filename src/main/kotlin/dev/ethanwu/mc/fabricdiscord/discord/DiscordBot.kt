package dev.ethanwu.mc.fabricdiscord.discord

import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import dev.ethanwu.mc.fabricdiscord.minecraft.message.ChatMessage
import dev.ethanwu.mc.fabricdiscord.minecraft.message.Message
import dev.ethanwu.mc.fabricdiscord.minecraft.message.SourceInfo
import dev.ethanwu.mc.fabricdiscord.util.Avatar
import dev.ethanwu.mc.fabricdiscord.util.functional.toCommon
import dev.ethanwu.mc.fabricdiscord.util.on
import dev.ethanwu.mc.fabricdiscord.util.orElseNull
import dev.ethanwu.mc.fabricdiscord.util.reactor.groupByType
import dev.ethanwu.mc.fabricdiscord.util.spec
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Webhook
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import org.apache.logging.log4j.LogManager
import reactor.core.Disposable
import reactor.core.Disposables
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private typealias OutgoingSender = (Flux<Message>) -> Mono<Void>

class DiscordBot(config: ServerConfig.DiscordConfig) : Disposable {
    companion object {
        val LOGGER = LogManager.getLogger()!!
    }

    private val heldResources = Disposables.composite()

    private val discordClient: DiscordClient = DiscordClientBuilder.create(config.botToken)
        .build()

    private val gatewayClient: Mono<GatewayDiscordClient> = discordClient.login().doOnNext {
        LOGGER.info("Connected to Discord gateway")
    }.cache()

    init {
        heldResources.add { gatewayClient.flatMap { it.logout() }.subscribe() }
    }

    private val configResolver: ConfigResolver = ConfigResolver(gatewayClient, config)

    // FIXME: figure out class hierarchy
    class DiscordBridgeInboundMessage(override val contents: String, sourceName: String) : ChatMessage {
        override val source: SourceInfo = SourceInfo(sourceName, null)
        override val flattened: String
            get() = "${source.name} » $contents"
    }

    fun onChatMessage(): Flux<ChatMessage> = configResolver.selfIds.flatMapMany { selfIds ->
        configResolver.resolvedChannelsConfig.map { it.inboundMessageChannels }
            .flatMapMany { channelConfigs ->
                val channelIds = channelConfigs.map { it.channel.id }.toSet()
                gatewayClient.flatMapMany { it.eventDispatcher.on<MessageCreateEvent>() }
                    .filter { it.message.channelId in channelIds }
            }
            .filter { Snowflake.of(it.message.userData.id()) !in selfIds }
            .map { event ->
                // FIXME: use proper type
                DiscordBridgeInboundMessage(
                    contents = event.message.content,
                    sourceName = event.member.orElseNull()?.displayName
                        ?: event.message.userData.username()
                        ?: run {
                            LOGGER.warn("Received message from unknown member ({})", event)
                            "<Discord user>"
                        },
                )
            }
    }

    private fun createBotOutgoingSender(channel: MessageChannel): OutgoingSender = { messageFlux ->
        messageFlux.flatMap { message ->
            channel.createMessage(spec {
                setContent(message.flattened)
            })
        }.then()
    }

    private fun createWebhookOutgoingSender(webhook: Webhook): (Flux<ChatMessage>) -> Mono<Void> = { messageFlux ->
        messageFlux.flatMap { message ->
            webhook.execute(spec {
                setContent(message.contents)
                setUsername(message.source.name)
                message.source.uuid?.let { uuid ->
                    setAvatarUrl(Avatar.avatarForUuid(uuid))
                }
            })
        }.then()
    }

    private fun createOutgoingSender(
        channelConfig: ConfigResolver.ResolvedChannelConfig,
    ): OutgoingSender =
        if (channelConfig.webhook != null) {
            { messageFlux ->
                messageFlux.groupByType(ChatMessage::class)
                    .flatMap {
                        it.bimap(
                            { otherMessages -> createBotOutgoingSender(channelConfig.channel)(otherMessages) },
                            { chatMessages -> createWebhookOutgoingSender(channelConfig.webhook)(chatMessages) }
                        ).toCommon()
                    }.then()
            }
        } else {
            createBotOutgoingSender(channelConfig.channel)
        }

    // TODO: consider returning Disposable instead of Mono<Void>
    fun sendOutgoingMessages(messages: Flux<Message>): Mono<Void> =
        configResolver.resolvedChannelsConfig.map { it.outboundMessageChannels }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { channelConfig ->
                createOutgoingSender(channelConfig)(messages)
            }.then()

    override fun dispose() {
        heldResources.dispose()
    }
}
