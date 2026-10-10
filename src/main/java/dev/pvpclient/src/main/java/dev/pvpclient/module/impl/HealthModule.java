package dev.pvpclient.module.impl;

import dev.pvpclient.Config;
import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

import java.util.Locale;

/** Player health, with absorption shown separately when active. */
public final class HealthModule extends TextModule {
    public HealthModule() {
        super("health", "Health", Anchor.TOP_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null && !Boolean.TRUE.equals(Config.data.vanillaHealthBar);
    }

    @Override
    protected String line(Minecraft mc) {
        if (mc.player == null) return "Health: --";
        float health = mc.player.getHealth();
        float maxHealth = mc.player.getMaxHealth();
        float absorption = mc.player.getAbsorptionAmount();
        String value = VitalDisplay.format("Health", health, maxHealth, Config.data.healthFormat, "hearts");
        return absorption > 0.0f ? value + String.format(Locale.ROOT, "  +%.0f absorption", absorption) : value;
    }
}
