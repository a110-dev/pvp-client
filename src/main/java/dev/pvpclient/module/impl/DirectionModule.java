package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

import java.util.Locale;

/** Compass-style facing readout: cardinal direction plus yaw. */
public final class DirectionModule extends TextModule {
    public DirectionModule() {
        super("direction", "Direction", Anchor.BOTTOM_LEFT, true);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null;
    }

    @Override
    protected String line(Minecraft mc) {
        if (mc.player == null) {
            return "";
        }
        String name = mc.player.getDirection().getName();
        String cardinal = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        float yaw = mc.player.getYRot() % 360f;
        if (yaw > 180f) {
            yaw -= 360f;
        } else if (yaw < -180f) {
            yaw += 360f;
        }
        return String.format(Locale.ROOT, "%s (%.0f)", cardinal, yaw);
    }
}
