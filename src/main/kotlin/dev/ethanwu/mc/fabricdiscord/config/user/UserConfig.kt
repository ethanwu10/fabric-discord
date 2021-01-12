package dev.ethanwu.mc.fabricdiscord.config.user

import kotlinx.serialization.Serializable

@Serializable
data class UserConfig(
    val formatting: UserFormattingConfig = UserFormattingConfig(),
    val discord: UserDiscordConfig? = null,
)
