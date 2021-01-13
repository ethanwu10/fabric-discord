package dev.ethanwu.mc.fabricdiscord

import dev.ethanwu.mc.fabricdiscord.config.ConfigLoader
import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import reactor.util.Loggers
import dev.ethanwu.mc.fabricdiscord.util.reactor.Logger as ReactorCustomLogger

class FabricDiscord : DedicatedServerModInitializer {
    companion object {
        val LOGGER = LogManager.getLogger()!!

        lateinit var config: ServerConfig
    }

    override fun onInitializeServer() {
        Loggers.useCustomLoggers(ReactorCustomLogger.factory)
        // load config early so we trigger validation early in startup
        config = ConfigLoader(FabricLoader.getInstance()).loadOrCreate()
        // Main initialization happens at server setup
    }
}
