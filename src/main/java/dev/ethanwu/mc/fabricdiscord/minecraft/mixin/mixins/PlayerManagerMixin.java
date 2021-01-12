package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.mixins;

import dev.ethanwu.mc.fabricdiscord.minecraft.mixin.impl.PlayerManagerMixinImplKt;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void fabricDiscordOnBroadcastChatMessage(Text text, MessageType messageType, UUID uuid, CallbackInfo info) {
        PlayerManagerMixinImplKt.onBroadcastChatMessage((PlayerManager) (Object) this, text, messageType, uuid, info);
    }
}
