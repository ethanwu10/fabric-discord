package dev.ethanwu.mc.fabricdiscord.logic

import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import java.util.*

class Core {
    companion object {
        fun onMinecraftChat(server: MinecraftServer, text: Text, type: MessageType, uuid: UUID) {

        }

        fun sendMinecraftChat(server: MinecraftServer, text: Text) {
            MinecraftChat.broadcastMessage(server, text, MessageType.CHAT, net.minecraft.util.Util.NIL_UUID)
        }
    }
}
