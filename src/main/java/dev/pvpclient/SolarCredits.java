package dev.pvpclient;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;

/** Small client signature and runtime label for the title screen, HUD, and settings menu. */
public final class SolarCredits {
    public static final String BRAND = "Solar Client Standalone";
    public static final String CREDIT = "Created by aleksa04_1";
    private static final int ACCENT = 0xFFFF8C00;
    private static final int TEXT = 0xFFF1F2F5;
    private static final int BACKGROUND = 0xB0101218;

    public static void registerTitleOverlay() {
        ScreenEvents.AFTER_INIT.register((client, screen, screenWidth, screenHeight) -> {
            if (!(screen instanceof TitleScreen)) return;
            ScreenEvents.afterForeground(screen).register((current, graphics, mouseX, mouseY, partialTick) -> {
                drawTitleSignature(graphics);
            });
        });
    }

    public static void renderHud(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        String brand = BRAND + "  |  " + CREDIT;
        int x = (graphics.guiWidth() - client.font.width(brand)) / 2;
        drawSignature(graphics, Math.max(4, x), 6, true);
    }

    public static String runtimeLine() {
        return VersionDetector.detect().displayLine();
    }

    private static void drawTitleSignature(GuiGraphicsExtractor graphics) {
        drawSignature(graphics, 9, 9, false);
    }

    private static void drawSignature(GuiGraphicsExtractor graphics, int x, int y, boolean compact) {
        Minecraft client = Minecraft.getInstance();
        String brand = BRAND + "  |  " + CREDIT;
        String runtime = runtimeLine();
        int width = Math.max(client.font.width(brand), client.font.width(runtime));
        int height = compact ? client.font.lineHeight : client.font.lineHeight * 2 + 3;
        graphics.fill(x - 4, y - 3, x + width + 4, y + height + 3, BACKGROUND);
        graphics.text(client.font, brand, x, y, ACCENT, true);
        if (!compact) {
            graphics.text(client.font, runtime, x, y + client.font.lineHeight + 3, TEXT, true);
        }
    }

    private SolarCredits() {
    }
}
