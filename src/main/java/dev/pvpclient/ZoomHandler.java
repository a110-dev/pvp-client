package dev.pvpclient;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Hold-to-zoom by temporarily lowering the FOV option. Uses only the public
 * options API (no mixins), so it survives game updates. The original FOV is
 * restored on release, on opening a screen, and when the client shuts down.
 */
public final class ZoomHandler {
    private final KeyMapping key;
    private boolean zooming;
    private boolean restoring;
    private int savedFov;
    private float currentFov;

    public static boolean scrollAdjust(Minecraft mc, double scrollAmount) {
        if (mc.player == null || mc.gui.screen() != null || !KeybindManager.zoomKey.isDown() || scrollAmount == 0.0) {
            return false;
        }
        int direction = scrollAmount > 0.0 ? -1 : 1;
        Config.data.zoomFov = Config.clamp(Config.data.zoomFov + direction * 5, 30, 110);
        Config.save();
        ToastManager.show("Zoom FOV: " + Config.data.zoomFov + "°");
        return true;
    }

    public ZoomHandler(KeyMapping key) {
        this.key = key;
    }

    public void tick(Minecraft mc) {
        boolean want = key.isDown() && mc.gui.screen() == null && mc.player != null;
        if (want && !zooming) {
            if (!restoring) {
                savedFov = mc.options.fov().get();
                currentFov = savedFov;
            }
            zooming = true;
            restoring = false;
        } else if (!want && zooming) {
            zooming = false;
            restoring = true;
        }

        if (zooming) {
            currentFov = lerp(currentFov, Config.clamp(Config.data.zoomFov, 30, 110), 0.32f);
            mc.options.fov().set(Math.round(currentFov));
        } else if (restoring) {
            currentFov = lerp(currentFov, savedFov, 0.34f);
            if (Math.abs(currentFov - savedFov) < 0.75f) {
                mc.options.fov().set(savedFov);
                currentFov = savedFov;
                restoring = false;
            } else {
                mc.options.fov().set(Math.round(currentFov));
            }
        }
    }

    public void reset(Minecraft mc) {
        if (zooming) {
            mc.options.fov().set(savedFov);
            zooming = false;
        }
        if (restoring) {
            mc.options.fov().set(savedFov);
            restoring = false;
        }
    }

    private static float lerp(float current, float target, float amount) {
        return current + (target - current) * amount;
    }
}
