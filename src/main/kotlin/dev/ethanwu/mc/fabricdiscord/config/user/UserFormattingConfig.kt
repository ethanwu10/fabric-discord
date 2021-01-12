package dev.ethanwu.mc.fabricdiscord.config.user

import kotlinx.serialization.Serializable

@Serializable
data class UserFormattingConfig(
    val enabled: Boolean = true,
    val breakLines: Boolean = false,
)
