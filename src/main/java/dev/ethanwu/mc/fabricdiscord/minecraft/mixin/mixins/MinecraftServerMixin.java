package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.mixins;

import dev.ethanwu.mc.fabricdiscord.minecraft.ServerInstance;
import dev.ethanwu.mc.fabricdiscord.minecraft.mixin.extensions.MinecraftServerMixinExtension;
import dev.ethanwu.mc.fabricdiscord.minecraft.mixin.impl.MinecraftServerMixinImplKt;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerMixinExtension {
    private ServerInstance fabricDiscordData;

    @NotNull
    @Override
    public ServerInstance getFabricDiscordServerInstance() {
        return fabricDiscordData;
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"))
    private void fabricDiscordOnSetupServer(CallbackInfo info) {
        fabricDiscordData = new ServerInstance((MinecraftServer) (Object) this);
        MinecraftServerMixinImplKt.onSetupServer((MinecraftServer) (Object) this, info);
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void fabricDiscordOnShutdown(CallbackInfo info) {
        MinecraftServerMixinImplKt.onShutdown((MinecraftServer) (Object) this, info);
    }
}
