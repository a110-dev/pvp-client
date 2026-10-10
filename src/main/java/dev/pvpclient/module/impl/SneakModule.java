package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

public final class SneakModule extends TextModule {
    public SneakModule() {
        super("sneak", "Sneak Status", Anchor.BOTTOM_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null;
    }

    @Override
    protected String line(Minecraft mc) {
        return "Sneak: " + (mc.player != null && mc.player.isCrouching() ? "ON" : "OFF");
    }
}
