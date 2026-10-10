package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

public final class FpsModule extends TextModule {
    public FpsModule() {
        super("fps", "FPS", Anchor.TOP_LEFT, true);
    }

    @Override
    protected String line(Minecraft mc) {
        return "FPS: " + mc.getFps();
    }
}
