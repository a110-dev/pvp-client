package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class SpeedModule extends TextModule {
    public SpeedModule() {
        super("speed", "Speed", Anchor.BOTTOM_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null;
    }

    @Override
    protected String line(Minecraft mc) {
        if (mc.player == null) return "Speed: --";
        Vec3 velocity = mc.player.getDeltaMovement();
        double blocksPerSecond = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
        return String.format(Locale.ROOT, "Speed: %.2f b/s", blocksPerSecond);
    }
}
