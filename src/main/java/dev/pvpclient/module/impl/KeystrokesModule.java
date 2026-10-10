package dev.pvpclient.module.impl;

import dev.pvpclient.ClickTracker;
import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** WASD, mouse buttons (with CPS) and space bar. */
public final class KeystrokesModule extends Module {
    private static final int BOX = 22;
    private static final int GAP = 2;
    private static final int W = BOX * 3 + GAP * 2;
    private static final int SPACE_H = 12;
    private static final int H = BOX * 3 + SPACE_H + GAP * 3;

    private static final int PRESSED_BG = 0xB0FFFFFF;
    private static final int BLACK = 0xFF000000;

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes", Anchor.BOTTOM_RIGHT, true);
    }

    @Override
    public int width(Minecraft mc) {
        return W;
    }

    @Override
    public int height(Minecraft mc) {
        return H;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        // Row 1: W
        key(g, mc, x + BOX + GAP, y, BOX, BOX, "W", mc.options.keyUp, null);
        // Row 2: A S D
        int y2 = y + BOX + GAP;
        key(g, mc, x, y2, BOX, BOX, "A", mc.options.keyLeft, null);
        key(g, mc, x + BOX + GAP, y2, BOX, BOX, "S", mc.options.keyDown, null);
        key(g, mc, x + (BOX + GAP) * 2, y2, BOX, BOX, "D", mc.options.keyRight, null);
        // Row 3: LMB RMB with CPS
        int y3 = y2 + BOX + GAP;
        int half = (W - GAP) / 2;
        key(g, mc, x, y3, half, BOX, "LMB", mc.options.keyAttack, ClickTracker.left() + " CPS");
        key(g, mc, x + half + GAP, y3, half, BOX, "RMB", mc.options.keyUse, ClickTracker.right() + " CPS");
        // Row 4: space bar
        int y4 = y3 + BOX + GAP;
        boolean space = mc.options.keyJump.isDown();
        g.fill(x, y4, x + W, y4 + SPACE_H, space ? PRESSED_BG : backgroundColor());
        int barY = y4 + SPACE_H / 2;
        int barColor = space ? BLACK : WHITE;
        g.fill(x + W / 2 - 10, barY, x + W / 2 + 10, barY + 1, barColor);
    }

    private static void key(GuiGraphicsExtractor g, Minecraft mc, int x, int y, int w, int h,
                            String label, KeyMapping mapping, String sub) {
        boolean down = mapping.isDown();
        g.fill(x, y, x + w, y + h, down ? PRESSED_BG : backgroundColor());
        int color = down ? BLACK : WHITE;

        int lineH = mc.font.lineHeight;
        int total = sub == null ? lineH : lineH * 2 + 1;
        int ty = y + (h - total) / 2;
        text(g, mc, label, x + (w - mc.font.width(label)) / 2, ty, color);
        if (sub != null) {
            text(g, mc, sub, x + (w - mc.font.width(sub)) / 2, ty + lineH + 1, color);
        }
    }
}
