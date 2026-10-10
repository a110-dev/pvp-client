package dev.pvpclient;

import dev.pvpclient.module.Module;
import dev.pvpclient.module.ModuleManager;
import dev.pvpclient.module.impl.ToggleSprintModule;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/** Central registration and rebinding for the client menu, zoom, and module toggles. */
public final class KeybindManager {
    public static KeyMapping menuKey;
    public static KeyMapping zoomKey;
    private static KeyMapping.Category category;
    private static final Map<String, KeyMapping> MODULE_KEYS = new LinkedHashMap<>();

    public static void init(KeyMapping.Category keyCategory) {
        category = keyCategory;
        menuKey = register("key.aleksa04_1.menu", Config.data.menuKeyCode);
        zoomKey = register("key.aleksa04_1.zoom", Config.data.zoomKeyCode);

        for (Module module : ModuleManager.all()) {
            KeyMapping binding = register("key.aleksa04_1.module." + module.id, module.keyCode);
            module.keyBinding = binding;
            MODULE_KEYS.put(module.id, binding);
        }
        KeyMapping.resetMapping();
    }

    private static KeyMapping register(String name, int keyCode) {
        int initial = keyCode < 0 ? InputConstants.KEY_ESCAPE : keyCode;
        KeyMapping mapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                name, InputConstants.Type.KEYBOARD, initial, category));
        mapping.setKey(toKey(keyCode));
        return mapping;
    }

    private static InputConstants.Key toKey(int keyCode) {
        return keyCode < 0 ? InputConstants.UNKNOWN : InputConstants.Type.KEYBOARD.getOrCreate(keyCode);
    }

    public static void bind(String target, int keyCode) {
        if ("$menu".equals(target)) {
            Config.data.menuKeyCode = keyCode;
            menuKey.setKey(toKey(keyCode));
        } else if ("$zoom".equals(target)) {
            Config.data.zoomKeyCode = keyCode;
            zoomKey.setKey(toKey(keyCode));
        } else {
            Module module = ModuleManager.get(target);
            module.keyCode = keyCode;
            module.keyBinding.setKey(toKey(keyCode));
        }
        KeyMapping.resetMapping();
        ModuleManager.save();
    }

    public static KeyMapping bindingFor(Module module) {
        return MODULE_KEYS.get(module.id);
    }

    public static void toggleMenu(Minecraft mc) {
        if (mc.gui.screen() instanceof HudEditorScreen editor) {
            editor.onClose();
        } else if (mc.gui.screen() instanceof ModuleScreen menu) {
            menu.onClose();
        } else if (mc.gui.screen() == null) {
            mc.gui.setScreen(new ModuleScreen(null));
        }
    }

    public static String label(KeyMapping mapping) {
        if (mapping == null || mapping.isUnbound()) return "UNBOUND";
        return mapping.getTranslatedKeyMessage().getString().toUpperCase(java.util.Locale.ROOT);
    }

    public static void tickModules(Minecraft mc) {
        if (mc.gui.screen() != null) return;
        boolean changed = false;
        for (Module module : ModuleManager.all()) {
            KeyMapping binding = MODULE_KEYS.get(module.id);
            if (binding != null && binding.consumeClick()) {
                module.enabled = !module.enabled;
                ToastManager.show(module.name + (module.enabled ? " enabled" : " disabled"));
                if (module.id.equals("performance")) {
                    PerformanceProfile.setEnabled(mc, module.enabled);
                }
                changed = true;
            }
            if (module instanceof ToggleSprintModule sprint) {
                sprint.tick(mc);
            }
        }
        if (changed) ModuleManager.save();
    }

    private KeybindManager() {
    }
}
