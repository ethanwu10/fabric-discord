package dev.ethanwu.mc.fabricdiscord.config

import dev.ethanwu.mc.fabricdiscord.config.user.UserConfig
import dev.ethanwu.mc.fabricdiscord.config.user.UserDiscordAdminChannelConfig
import dev.ethanwu.mc.fabricdiscord.config.user.UserDiscordChannelConfig
import discord4j.common.util.Snowflake
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ConfigLoader(private val fabricLoader: FabricLoader) {
    companion object {
        val LOGGER = LogManager.getLogger()!!

        private val webhookUrlRegex = Regex("https://[^/]+/api/webhooks/([0-9]+)/([a-zA-Z0-9_-]+)")
    }

    private val configRelPath = fabricLoader.configDir.fileSystem.getPath("discord-config.json")
    private val configFile = fabricLoader.configDir.resolve(configRelPath).toFile()

    private val jsonFormat = Json { prettyPrint = true }

    private fun loadRaw(): UserConfig? = when {
        !configFile.exists() -> null
        else -> jsonFormat.decodeFromString(configFile.readText())
    }

    private fun loadOrCreateRaw(): UserConfig = loadRaw() ?: run {
        LOGGER.info("Cannot find config, creating file with defaults")
        UserConfig().also {
            configFile.writeText(jsonFormat.encodeToString(it))
        }
    }

    private fun UserDiscordChannelConfig.transform(): ServerConfig.DiscordConfig.ChannelConfig {
        require(channelId != null || (webhookId != null && webhookToken != null) || webhookUrl != null) {
            "one of channelId, webhookId and webhookToken, or webhookUrl must be provided"
        }
        return ServerConfig.DiscordConfig.ChannelConfig(
            channelId = channelId?.let { Snowflake.of(it) },
            webhook = when {
                webhookId != null && webhookToken != null -> ServerConfig.DiscordConfig.ChannelConfig.WebhookInfo(
                    id = Snowflake.of(webhookId),
                    token = webhookToken
                )
                webhookUrl != null -> {
                    val match = webhookUrlRegex.matchEntire(webhookUrl)
                    require(match != null) { "Invalid webhook URL" }
                    // TODO: handle unsigned correctly
                    val parsedId = match.groups[1]!!.value.toLongOrNull()
                    require(parsedId != null) { "Invalid webhook URL" }
                    val parsedToken = match.groups[2]!!.value
                    ServerConfig.DiscordConfig.ChannelConfig.WebhookInfo(
                        id = Snowflake.of(parsedId),
                        token = parsedToken
                    )
                }
                else -> null
            }
        )
    }

    private fun UserDiscordAdminChannelConfig.transform(): ServerConfig.DiscordConfig.AdminChannelConfig =
        ServerConfig.DiscordConfig.AdminChannelConfig(
            channel = channel.transform(),
            includeMessages = includeMessages
        )

    private fun UserConfig.transform(): ServerConfig = ServerConfig(
        formattingConfig = if (formatting.enabled) {
            ServerConfig.FormattingConfig(breakLines = formatting.breakLines)
        } else {
            null
        },
        discordConfig = discord?.let {
            ServerConfig.DiscordConfig(
                botToken = it.botToken,
                messageChannels = it.messageChannels.map { it.transform() },
                adminChannels = it.adminChannels.map { it.transform() }
            )
        }
    )

    fun load(): ServerConfig? = loadRaw()?.transform()
    fun loadOrCreate(): ServerConfig = loadOrCreateRaw().transform()
}
