package dev.ethanwu.mc.fabricdiscord.config.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDiscordAdminChannelConfig(
    val channel: UserDiscordChannelConfig = UserDiscordChannelConfig(),
    val includeMessages: Boolean = true,
)

