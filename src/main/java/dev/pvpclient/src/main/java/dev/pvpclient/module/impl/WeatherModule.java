package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

public final class WeatherModule extends TextModule {
    public WeatherModule() {
        super("weather", "Weather", Anchor.TOP_RIGHT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.level != null;
    }

    @Override
    protected String line(Minecraft mc) {
        return mc.level == null ? "Weather: --" : "Weather: " + (mc.level.isRaining() ? "Rain" : "Clear");
    }
}
