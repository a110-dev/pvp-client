package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Displays current server name and IP. */
public final class ServerModule extends Module {
    public ServerModule() {
        super("server", "Server Info", Anchor.TOP_RIGHT, false);
    }

    @Override
    public int width(Minecraft mc) {
        return 120;
    }

    @Override
    public int height(Minecraft mc) {
        return 22;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        g.fill(x, y, x + width(mc), y + height(mc), backgroundColor());
        if (mc.getCurrentServer() != null) {
            String serverName = mc.getCurrentServer().name;
            if (serverName != null && serverName.length() > 15) {
                serverName = serverName.substring(0, 14) + "…";
            }
            text(g, mc, serverName != null ? serverName : "Singleplayer", x + padding, y + padding, WHITE);
        } else {
            text(g, mc, "Singleplayer", x + padding, y + padding, WHITE);
        }
    }
}
