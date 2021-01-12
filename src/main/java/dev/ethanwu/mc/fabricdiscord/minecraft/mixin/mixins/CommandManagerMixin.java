package dev.ethanwu.mc.fabricdiscord.minecraft.mixin.mixins;

import dev.ethanwu.mc.fabricdiscord.minecraft.mixin.impl.CommandManagerMixinImplKt;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {

    // Injection point is technically unsupported behavior!
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V"))
    private void fabricDiscordOnConstruct(
            CommandManager.RegistrationEnvironment registrationEnvironment,
            CallbackInfo callbackInfo
    ) {
        CommandManagerMixinImplKt.onConstruct((CommandManager) (Object) this, registrationEnvironment, callbackInfo);
    }
}
