package dev.pvpclient.mixin;

import dev.pvpclient.ZoomHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Routes the wheel to zoom levels while the zoom key is held. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true)
    private void aleksa04_1$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ZoomHandler.scrollAdjust(Minecraft.getInstance(), vertical)) ci.cancel();
    }
}
