package dev.pvpclient.mixin;

import dev.pvpclient.module.ModuleManager;
import dev.pvpclient.Config;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces vanilla hearts and food only while their movable client widgets are enabled. */
@Mixin(Hud.class)
abstract class VitalHudMixin {
    @Inject(method = "extractHearts", at = @At("HEAD"), cancellable = true)
    private void pvpclient$hideVanillaHearts(CallbackInfo ci) {
        if (ModuleManager.isEnabled("health") && !Boolean.TRUE.equals(Config.data.vanillaHealthBar)) ci.cancel();
    }

    @Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
    private void pvpclient$hideVanillaFood(CallbackInfo ci) {
        if (ModuleManager.isEnabled("hunger") && !Boolean.TRUE.equals(Config.data.vanillaFoodBar)) ci.cancel();
    }
}
