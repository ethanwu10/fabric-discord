package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.mixins;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommandSource.class)
public abstract class ServerCommandSourceMixin {
    @Inject(method = "sendToOps", at = @At("HEAD"))
    private void fabricDiscordOnSendToOpsSendServerSystemMessage(Text text, CallbackInfo callbackInfo) {

    }
}
