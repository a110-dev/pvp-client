package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

/**
 * Toggle Sprint. While this module is enabled, holding forward sprints without holding the
 * sprint key. The logic lives in {@link #tick}, the HUD line just shows that it is active.
 */
public final class ToggleSprintModule extends TextModule {
    public ToggleSprintModule() {
        super("sprint", "Toggle Sprint", Anchor.BOTTOM_LEFT, true);
        keyCode = InputConstants.KEY_K;
    }

    @Override
    protected String line(Minecraft mc) {
        return "Sprint: Toggled";
    }

    public void tick(Minecraft mc) {
        if (!enabled || mc.player == null || mc.gui.screen() != null) {
            return;
        }
        if (mc.options.keyUp.isDown()) {
            mc.options.keySprint.setDown(true);
        }
    }
}
