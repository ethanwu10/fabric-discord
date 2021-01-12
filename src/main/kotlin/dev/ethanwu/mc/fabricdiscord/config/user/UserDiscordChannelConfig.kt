package dev.ethanwu.mc.fabricdiscord.config.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDiscordChannelConfig(
    val channelId: Long? = null,
    val webhookUrl: String? = null,
    val webhookId: Long? = null,
    val webhookToken: String? = null,
)
