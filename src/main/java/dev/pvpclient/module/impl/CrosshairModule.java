package dev.pvpclient.module.impl;

import dev.pvpclient.Config;
import dev.pvpclient.ClickTracker;
import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Optional minimal crosshair overlay, centered by ModuleManager. */
public final class CrosshairModule extends Module {
    public CrosshairModule() {
        super("crosshair", "Custom Crosshair", Anchor.TOP_LEFT, false);
    }

    @Override
    public int width(Minecraft mc) {
        return 1;
    }

    @Override
    public int height(Minecraft mc) {
        return 1;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        int length = Config.clamp(Config.data.crosshairSize, 2, 12);
        long attackAge = ClickTracker.attackAgeMillis();
        int bloom = Boolean.TRUE.equals(Config.data.dynamicCrosshair) && attackAge < 220
                ? Math.round(5.0f * (1.0f - attackAge / 220.0f)) : 0;
        int gap = Config.clamp(Config.data.crosshairGap, 0, 12) + bloom;
        int color = accentColor();
        switch (Config.clamp(Config.data.crosshairShape, 0, 2)) {
            case 1 -> g.fill(x - 1, y - 1, x + 2, y + 2, color);
            case 2 -> drawRing(g, x, y, length, color);
            default -> {
                g.fill(x - gap - length, y - 1, x - gap, y + 1, color);
                g.fill(x + gap + 1, y - 1, x + gap + length + 1, y + 1, color);
                g.fill(x - 1, y - gap - length, x + 1, y - gap, color);
                g.fill(x - 1, y + gap + 1, x + 1, y + gap + length + 1, color);
            }
        }
    }

    private static void drawRing(GuiGraphicsExtractor g, int x, int y, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int outer = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            int inner = Math.max(0, radius - 2);
            int innerX = Math.abs(dy) > inner ? -1 : (int) Math.round(Math.sqrt(inner * inner - dy * dy));
            g.fill(x - outer, y + dy, x - innerX, y + dy + 1, color);
            if (innerX >= 0) g.fill(x + innerX + 1, y + dy, x + outer + 1, y + dy + 1, color);
            else g.fill(x + 1, y + dy, x + outer + 1, y + dy + 1, color);
        }
    }
}
