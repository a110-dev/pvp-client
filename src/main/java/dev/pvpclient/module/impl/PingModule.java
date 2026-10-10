package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class PingModule extends TextModule {
    public PingModule() {
        super("ping", "Ping", Anchor.TOP_LEFT, true);
    }

    @Override
    protected String line(Minecraft mc) {
        ClientPacketListener connection = mc.getConnection();
        if (connection != null && mc.player != null) {
            PlayerInfo info = connection.getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                return "Ping: " + info.getLatency() + " ms";
            }
        }
        return "Ping: --";
    }
}
