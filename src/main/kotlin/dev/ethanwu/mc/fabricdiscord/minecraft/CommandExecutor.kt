package dev.ethanwu.mc.fabricdiscord.minecraft

import dev.ethanwu.mc.fabricdiscord.config.ServerConfig
import dev.ethanwu.mc.fabricdiscord.minecraft.message.CommandFeedbackSystemMessage
import dev.ethanwu.mc.fabricdiscord.minecraft.message.SourceInfo
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandOutput
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.*

class CommandExecutor(
    private val server: MinecraftServer,
    private val config: ServerConfig,
) {
    class CommandExecutorCommandOutput(
        private val displayName: Text,
        private val sink: Sinks.Many<CommandFeedbackSystemMessage>,
        private val shouldBroadcast: Boolean,
    ) : CommandOutput {
        override fun sendSystemMessage(text: Text, uuid: UUID) {
            // FIXME: make this less hacky
            val feedbackText = TranslatableText(
                "chat.type.admin",
                displayName,
                text
            ).formatted(Formatting.GRAY, Formatting.ITALIC) as TranslatableText
            // TODO: add retry implementation?
            sink.emitNext(CommandFeedbackSystemMessage(feedbackText), Sinks.EmitFailureHandler.FAIL_FAST)
        }

        override fun shouldReceiveFeedback(): Boolean = true

        override fun shouldTrackOutput(): Boolean = true

        override fun shouldBroadcastConsoleToOps(): Boolean = shouldBroadcast
    }

    private val feedbackSink: Sinks.Many<CommandFeedbackSystemMessage> = Sinks.many().multicast().onBackpressureBuffer()
    val feedbackFlux: Flux<CommandFeedbackSystemMessage> get() = feedbackSink.asFlux()

    private fun makeCommandSource(source: SourceInfo): ServerCommandSource {
        // TODO: unified display name Text creation
        val sourceDisplayName = LiteralText(source.name)
        val targetWorld = server.overworld
        return ServerCommandSource(
            // TODO: make shouldBroadcast configurable
            CommandExecutorCommandOutput(sourceDisplayName, feedbackSink, true),
            targetWorld?.let { Vec3d.of(it.spawnPos) } ?: Vec3d.ZERO,
            Vec2f.ZERO,
            targetWorld,
            // TODO: make op level configurable
            4,
            source.name,
            sourceDisplayName,
            server,
            null
        )
    }

    /**
     * Execute a command via the given source
     *
     * This must be called on the main server thread
     */
    fun executeCommand(source: SourceInfo, command: String) {
        server.commandManager.execute(makeCommandSource(source), command)
    }
}
