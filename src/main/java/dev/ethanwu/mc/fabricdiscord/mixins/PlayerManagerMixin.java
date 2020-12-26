package dev.ethanwu.mc.fabricdiscord.mixins;

import dev.ethanwu.mc.fabricdiscord.logic.MinecraftChat;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Final
    @Shadow
    private MinecraftServer server;

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(Text text, MessageType messageType, UUID uuid, CallbackInfo info) {
        MinecraftChat.Companion.onMixinHook(server, text, messageType, uuid, info);
    }
}
