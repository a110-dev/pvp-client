package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Locale;

/** Read-only distance to the entity currently under the crosshair. */
public final class TargetDistanceModule extends TextModule {
    public TargetDistanceModule() {
        super("targetdistance", "Target Distance", Anchor.TOP_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null && mc.hitResult instanceof EntityHitResult;
    }

    @Override
    protected String line(Minecraft mc) {
        if (mc.player == null || !(mc.hitResult instanceof EntityHitResult target)) return "Target: --";
        double distance = mc.player.getEyePosition().distanceTo(target.getEntity().position());
        return String.format(Locale.ROOT, "Target: %.2f blocks", distance);
    }
}
