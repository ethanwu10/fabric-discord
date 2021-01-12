package dev.ethanwu.mc.fabricdiscord.config.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDiscordConfig(
    val botToken: String,
    val messageChannels: List<UserDiscordChannelConfig>,
    val adminChannels: List<UserDiscordAdminChannelConfig> = listOf(),
)
