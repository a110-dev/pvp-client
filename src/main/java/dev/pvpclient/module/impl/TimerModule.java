package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Displays a session timer showing how long you've been playing. */
public final class TimerModule extends Module {
    private long sessionStart = System.currentTimeMillis();

    public TimerModule() {
        super("timer", "Session Timer", Anchor.TOP_LEFT, false);
    }

    @Override
    public int width(Minecraft mc) {
        return 95;
    }

    @Override
    public int height(Minecraft mc) {
        return 22;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        g.fill(x, y, x + width(mc), y + height(mc), backgroundColor());
        long elapsed = System.currentTimeMillis() - sessionStart;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / 60000) % 60;
        long hours = elapsed / 3600000;
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        text(g, mc, "Session: " + time, x + padding, y + padding, WHITE);
    }
}
