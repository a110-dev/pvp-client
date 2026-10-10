package dev.pvpclient.module.impl;

import dev.pvpclient.Config;
import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

import java.util.Locale;

/** Food and saturation values for survival play. */
public final class HungerModule extends TextModule {
    public HungerModule() {
        super("hunger", "Food & Saturation", Anchor.TOP_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null && !Boolean.TRUE.equals(Config.data.vanillaFoodBar);
    }

    @Override
    protected String line(Minecraft mc) {
        if (mc.player == null) return "Food: --";
        var food = mc.player.getFoodData();
        return VitalDisplay.format("Food", food.getFoodLevel(), 20.0f, Config.data.hungerFormat, "icons")
                + String.format(Locale.ROOT, "  Sat: %.1f", food.getSaturationLevel());
    }
}
