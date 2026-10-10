package dev.pvpclient.module.impl;

import dev.pvpclient.ClickTracker;
import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

public final class CpsModule extends TextModule {
    public CpsModule() {
        super("cps", "CPS", Anchor.TOP_LEFT, true);
    }

    @Override
    protected String line(Minecraft mc) {
        return "CPS: " + ClickTracker.left() + " | " + ClickTracker.right();
    }
}
