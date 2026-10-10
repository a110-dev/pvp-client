package dev.pvpclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Small animated feedback for module toggles. */
public final class ToastManager {
    private static String message = "";
    private static long shownAt;

    public static void show(String text) {
        message = text;
        shownAt = System.nanoTime();
    }

    public static void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (message.isEmpty()) return;
        long elapsedMs = (System.nanoTime() - shownAt) / 1_000_000L;
        if (elapsedMs >= 2100) {
            message = "";
            return;
        }

        float enter = Math.min(1.0f, elapsedMs / 180.0f);
        float leave = elapsedMs < 1850 ? 1.0f : Math.max(0.0f, (2100.0f - elapsedMs) / 250.0f);
        float eased = 1.0f - (1.0f - enter) * (1.0f - enter);
        int alpha = Math.round(235 * Math.min(eased, leave));
        int width = mc.font.width(message) + 28;
        int x = (g.guiWidth() - width) / 2;
        int y = 10 - Math.round((1.0f - eased) * 18.0f);
        g.fill(x, y, x + width, y + 30, (alpha << 24) | 0x00101824);
        g.fill(x, y, x + 3, y + 30, (alpha << 24) | (Config.data.accentColor() & 0x00FFFFFF));
        int textAlpha = Math.round(255 * Math.min(eased, leave));
        g.text(mc.font, message, x + 14, y + 10, (textAlpha << 24) | 0x00F1F5FA, false);
    }

    private ToastManager() {
    }
}
