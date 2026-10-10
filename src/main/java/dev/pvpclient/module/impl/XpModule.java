package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Displays current XP level and progress. */
public final class XpModule extends Module {
    public XpModule() {
        super("xp", "XP Level", Anchor.BOTTOM_LEFT, false);
    }

    @Override
    public int width(Minecraft mc) {
        return 75;
    }

    @Override
    public int height(Minecraft mc) {
        return 22;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        g.fill(x, y, x + width(mc), y + height(mc), backgroundColor());
        int level = mc.player.experienceLevel;
        float progress = mc.player.experienceProgress;
        text(g, mc, "Level: " + level, x + padding, y + padding, WHITE);
        text(g, mc, String.format("%.0f%%", progress * 100), x + padding, y + padding + 10, 0xFF78D9A1);
    }
}
