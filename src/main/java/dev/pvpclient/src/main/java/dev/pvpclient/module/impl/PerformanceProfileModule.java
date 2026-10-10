package dev.pvpclient.module.impl;

import dev.pvpclient.PerformanceProfile;
import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

public final class PerformanceProfileModule extends TextModule {
    public PerformanceProfileModule() {
        super("performance", "Performance Profile", Anchor.TOP_RIGHT, false);
    }

    @Override
    public String category() {
        return "PERFORMANCE";
    }

    @Override
    public String description() {
        return "Reversible low-effects graphics preset";
    }

    @Override
    protected String line(Minecraft mc) {
        return "Performance: " + (enabled ? "ON" : "OFF");
    }
}
