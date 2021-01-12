package dev.ethanwu.mc.fabricdiscord.discord

import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Webhook
import discord4j.core.`object`.entity.channel.MessageChannel
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.cast

class ConfigResolver(
    private val gatewayClient: Mono<GatewayDiscordClient>,
    private val config: ServerConfig.DiscordConfig,
) {

    val selfBotId: Mono<Snowflake> = gatewayClient.map { it.selfId }.cache()

    val selfIds: Mono<List<Snowflake>> = selfBotId.map { selfBotId ->
        listOf(selfBotId) + (config.messageChannels + config.adminChannels.map { it.channel })
            .mapNotNull { it.webhook?.id }
    }.cache()

    data class ResolvedChannelConfig(
        val channel: MessageChannel,
        val webhook: Webhook?,
        val original: ServerConfig.DiscordConfig.ChannelConfig,
    )

    data class ResolvedAdminChannelConfig(
        val channelConfig: ResolvedChannelConfig,
        val original: ServerConfig.DiscordConfig.AdminChannelConfig,
    )

    data class ResolvedChannelsConfig(
        /**
         * All channels where messages from Minecraft will be sent to Discord
         */
        val outboundMessageChannels: List<ResolvedChannelConfig>,
        /**
         * All channels where messages from Discord will be sent to Minecraft
         */
        val inboundMessageChannels: List<ResolvedChannelConfig>,
        /**
         * Channels where op logs are sent, and Discord messages are executed as
         * server commands
         */
        val adminChannels: List<ResolvedAdminChannelConfig>,
    )

    private fun ServerConfig.DiscordConfig.ChannelConfig.resolved(): Mono<ResolvedChannelConfig> =
        gatewayClient.flatMap { client ->
            if (webhook != null) {
                client.getWebhookByIdWithToken(webhook.id, webhook.token)
                    .flatMap { resolvedWebhook ->
                        when {
                            channelId != null && channelId != resolvedWebhook.channelId ->
                                Mono.error(IllegalArgumentException("Provided channelId does not match with webhook!"))
                            else -> resolvedWebhook.channel.map { resolvedChannel ->
                                ResolvedChannelConfig(
                                    channel = resolvedChannel,
                                    webhook = resolvedWebhook,
                                    original = this,
                                )
                            }
                        }
                    }
            } else {
                // The invariant that either both guildId and channelId are present, or
                // webhook is present is checked in the config loader
                client.getChannelById(channelId!!)
                    .cast<MessageChannel>()
                    .onErrorMap {
                        if (it is ClassCastException) {
                            IllegalArgumentException("Specified channel (${channelId}) was not a text channel!")
                        } else {
                            it
                        }
                    }
                    .map { channel ->
                        ResolvedChannelConfig(
                            channel = channel,
                            webhook = null,
                            original = this,
                        )
                    }
            }
        }

    private fun ServerConfig.DiscordConfig.AdminChannelConfig.resolved(): Mono<ResolvedAdminChannelConfig> =
        channel.resolved().map { channelConfig ->
            ResolvedAdminChannelConfig(
                channelConfig = channelConfig,
                original = this,
            )
        }

    @JvmName("resolvedChannelConfig")
    private fun List<ServerConfig.DiscordConfig.ChannelConfig>.resolved(): Mono<List<ResolvedChannelConfig>> =
        Flux.fromIterable(this)
            .flatMap { it.resolved() }
            .collectList()

    @JvmName("resolvedAdminChannelConfig")
    private fun List<ServerConfig.DiscordConfig.AdminChannelConfig>.resolved(): Mono<List<ResolvedAdminChannelConfig>> =
        Flux.fromIterable(this)
            .flatMap { it.resolved() }
            .collectList()

    val resolvedChannelsConfig: Mono<ResolvedChannelsConfig> = config.run {
        Mono.zip(
            messageChannels.resolved(),
            adminChannels.resolved()
        ) { messageChannels, adminChannels ->
            ResolvedChannelsConfig(
                outboundMessageChannels = messageChannels +
                        adminChannels.filter { it.original.includeMessages }
                            .map { it.channelConfig },
                inboundMessageChannels = messageChannels,
                adminChannels = adminChannels,
            )
        }.cache()
    }
}
