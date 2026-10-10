package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayDeque;

/** Compact rolling FPS graph. */
public final class FpsGraphModule extends Module {
    private static final int WIDTH = 124;
    private static final int HEIGHT = 48;
    private final ArrayDeque<Integer> samples = new ArrayDeque<>();

    public FpsGraphModule() {
        super("fpsgraph", "FPS Graph", Anchor.TOP_RIGHT, false);
    }

    @Override
    public int width(Minecraft mc) {
        return WIDTH;
    }

    @Override
    public int height(Minecraft mc) {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        samples.addLast(Math.max(0, mc.getFps()));
        while (samples.size() > WIDTH - 8) samples.removeFirst();

        g.fill(x, y, x + WIDTH, y + HEIGHT, backgroundColor());
        g.fill(x, y, x + 2, y + HEIGHT, accentColor());
        int baseline = y + HEIGHT - 5;
        int chartHeight = HEIGHT - 15;
        int index = 0;
        for (int fps : samples) {
            int bar = Math.max(2, Math.min(chartHeight, fps * chartHeight / 240));
            int color = fps >= 120 ? 0xFF5ED6B3 : fps >= 60 ? accentColor() : 0xFFFFB86B;
            g.fill(x + 5 + index, baseline - bar, x + 6 + index, baseline, color);
            index++;
        }
        text(g, mc, "FPS  " + mc.getFps(), x + 7, y + 4, WHITE);
    }
}
