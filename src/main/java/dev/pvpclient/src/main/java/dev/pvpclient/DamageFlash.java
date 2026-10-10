package dev.pvpclient;

import dev.pvpclient.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Brief visual feedback when the player starts an entity attack. */
public final class DamageFlash {
    private static long startedAt;

    public static void trigger() {
        startedAt = System.nanoTime();
    }

    public static void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (!ModuleManager.get("hitflash").enabled || startedAt == 0L) return;
        long age = (System.nanoTime() - startedAt) / 1_000_000L;
        if (age >= 150) {
            startedAt = 0L;
            return;
        }
        float envelope = age < 35 ? age / 35.0f : (150.0f - age) / 115.0f;
        int alpha = Math.round(70 * Math.max(0.0f, Math.min(1.0f, envelope)));
        int color = ModuleManager.get("hitflash").accentColor();
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), (alpha << 24) | (color & 0x00FFFFFF));
    }

    private DamageFlash() {
    }
}
