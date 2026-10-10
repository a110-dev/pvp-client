package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Toggle for a short customizable client-side attack flash. */
public final class HitFlashModule extends Module {
    public HitFlashModule() {
        super("hitflash", "Hit Color Flash", Anchor.TOP_LEFT, false);
    }

    @Override
    public int width(Minecraft mc) {
        return 0;
    }

    @Override
    public int height(Minecraft mc) {
        return 0;
    }

    @Override
    public boolean visible(Minecraft mc) {
        return false;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
    }
}
