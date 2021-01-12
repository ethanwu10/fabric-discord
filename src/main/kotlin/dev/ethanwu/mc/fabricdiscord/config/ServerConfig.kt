package dev.ethanwu.mc.fabricdiscord.config

import discord4j.common.util.Snowflake

data class ServerConfig(
    val formattingConfig: FormattingConfig?,
    val discordConfig: DiscordConfig?,
) {
    data class FormattingConfig(
        val breakLines: Boolean,
    )

    data class DiscordConfig(
        val botToken: String,
        val messageChannels: List<ChannelConfig>,
        val adminChannels: List<AdminChannelConfig>,
    ) {
        data class ChannelConfig(
            val channelId: Snowflake?,
            val webhook: WebhookInfo?,
        ) {
            init {
                require(channelId != null || webhook != null) { "Either webhook of channelId must be provided" }
            }

            data class WebhookInfo(
                val id: Snowflake,
                val token: String,
            )
        }

        data class AdminChannelConfig(
            val channel: ChannelConfig,
            val includeMessages: Boolean,
        )
    }
}
