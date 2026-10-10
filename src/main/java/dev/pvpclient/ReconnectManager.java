package dev.pvpclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/** Reopens the most recently joined multiplayer server. */
public final class ReconnectManager {
    public static boolean reconnect(Minecraft mc, Screen parent) {
        ServerData server = mc.getCurrentServer();
        if (server == null || mc.isLocalServer() || server.ip == null || server.ip.isBlank()) return false;
        ConnectScreen.startConnecting(parent, mc, ServerAddress.parseString(server.ip), server, false, null);
        return true;
    }

    private ReconnectManager() {
    }
}
