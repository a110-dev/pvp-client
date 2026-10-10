package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

import java.util.Locale;

public final class CoordsModule extends TextModule {
    public CoordsModule() {
        super("coords", "Coordinates", Anchor.BOTTOM_LEFT, true);
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
        return String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }
}
