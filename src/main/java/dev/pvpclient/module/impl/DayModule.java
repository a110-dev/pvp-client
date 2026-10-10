package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

public final class DayModule extends TextModule {
    public DayModule() {
        super("day", "World Day", Anchor.BOTTOM_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.level != null;
    }

    @Override
    protected String line(Minecraft mc) {
        return mc.level == null ? "Day: --" : "Day: " + (mc.level.getDefaultClockTime() / 24000L + 1L);
    }
}
