package dev.pvpclient.module;

import dev.pvpclient.Config;
import dev.pvpclient.module.impl.ArmorModule;
import dev.pvpclient.module.impl.ClockModule;
import dev.pvpclient.module.impl.CoordsModule;
import dev.pvpclient.module.impl.CrosshairModule;
import dev.pvpclient.module.impl.CpsModule;
import dev.pvpclient.module.impl.DayModule;
import dev.pvpclient.module.impl.DimensionModule;
import dev.pvpclient.module.impl.DirectionModule;
import dev.pvpclient.module.impl.FpsModule;
import dev.pvpclient.module.impl.FpsGraphModule;
import dev.pvpclient.module.impl.BiomeModule;
import dev.pvpclient.module.impl.DurabilityAlertModule;
import dev.pvpclient.module.impl.HealthModule;
import dev.pvpclient.module.impl.HeldItemModule;
import dev.pvpclient.module.impl.HungerModule;
import dev.pvpclient.module.impl.HitFlashModule;
import dev.pvpclient.module.impl.TargetDistanceModule;
import dev.pvpclient.module.impl.HotbarModule;
import dev.pvpclient.module.impl.ServerModule;
import dev.pvpclient.module.impl.TimerModule;
import dev.pvpclient.module.impl.XpModule;
import dev.pvpclient.module.impl.KeystrokesModule;
import dev.pvpclient.module.impl.MemoryModule;
import dev.pvpclient.module.impl.PingModule;
import dev.pvpclient.module.impl.PotionsModule;
import dev.pvpclient.module.impl.PerformanceProfileModule;
import dev.pvpclient.module.impl.SneakModule;
import dev.pvpclient.module.impl.SpeedModule;
import dev.pvpclient.module.impl.ToggleSprintModule;
import dev.pvpclient.module.impl.WeatherModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private static final int MARGIN = 4;
    private static final int GAP = 3;

    private static final List<Module> MODULES = new ArrayList<>();

    public record HudPosition(int x, int y, int width, int height) {
    }

    public static void init() {
        MODULES.clear();
        MODULES.add(new FpsModule());
        MODULES.add(new FpsGraphModule());
        MODULES.add(new CpsModule());
        MODULES.add(new PingModule());
        MODULES.add(new CoordsModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new ArmorModule());
        MODULES.add(new PotionsModule());
        MODULES.add(new DirectionModule());
        MODULES.add(new ToggleSprintModule());
        MODULES.add(new SneakModule());
        MODULES.add(new ClockModule());
        MODULES.add(new SpeedModule());
        MODULES.add(new DimensionModule());
        MODULES.add(new DayModule());
        MODULES.add(new WeatherModule());
        MODULES.add(new CrosshairModule());
        MODULES.add(new PerformanceProfileModule());
        MODULES.add(new HealthModule());
        MODULES.add(new HungerModule());
        MODULES.add(new BiomeModule());
        MODULES.add(new HeldItemModule());
        MODULES.add(new MemoryModule());
        MODULES.add(new DurabilityAlertModule());
        MODULES.add(new HitFlashModule());
        MODULES.add(new TargetDistanceModule());
        MODULES.add(new HotbarModule());
        MODULES.add(new ServerModule());
        MODULES.add(new TimerModule());
        MODULES.add(new XpModule());

        for (Module m : MODULES) {
            Config.Entry e = Config.data.modules.get(m.id);
            if (e != null) {
                if (e.enabled != null) m.enabled = e.enabled;
                if (e.anchor != null) m.anchor = Anchor.byName(e.anchor, m.defaultAnchor);
                if (e.keyCode != null) m.keyCode = e.keyCode;
                if (e.color != null) m.customColor = e.color;
                if (e.scale != null) m.scalePercent = Config.clamp(e.scale, 70, 150);
                if (e.x != null && e.y != null) {
                    m.customX = e.x;
                    m.customY = e.y;
                    m.hasCustomPosition = true;
                }
                if (e.customWidth != null) m.customWidth = e.customWidth;
                if (e.customHeight != null) m.customHeight = e.customHeight;
                if (e.padding != null) m.padding = Config.clamp(e.padding, 0, 20);
                if (e.borderRadius != null) m.borderRadius = Config.clamp(e.borderRadius, 0, 16);
                if (e.backgroundColor != null) m.backgroundColor = e.backgroundColor;
                if (e.showBorder != null) m.showBorder = e.showBorder;
                if (e.borderColor != null) m.borderColor = e.borderColor;
                if (e.opacity != null) m.opacity = Config.clamp(e.opacity, 0, 100);
            }
        }
    }

    public static List<Module> all() {
        return MODULES;
    }

    public static Module get(String id) {
        for (Module m : MODULES) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Unknown module: " + id);
    }

    public static boolean isEnabled(String id) {
        for (Module module : MODULES) {
            if (module.id.equals(id)) return module.enabled;
        }
        return false;
    }

    public static void save() {
        for (Module m : MODULES) {
            Config.Entry e = new Config.Entry();
            e.enabled = m.enabled;
            e.anchor = m.anchor.name();
            e.keyCode = m.keyCode;
            e.color = m.customColor < 0 ? null : m.customColor;
            e.scale = m.scalePercent;
            e.x = m.hasCustomPosition ? m.customX : null;
            e.y = m.hasCustomPosition ? m.customY : null;
            e.customWidth = m.customWidth < 0 ? null : m.customWidth;
            e.customHeight = m.customHeight < 0 ? null : m.customHeight;
            e.padding = m.padding != PAD ? m.padding : null;
            e.borderRadius = m.borderRadius != 0 ? m.borderRadius : null;
            e.backgroundColor = m.backgroundColor < 0 ? null : m.backgroundColor;
            e.showBorder = m.showBorder ? true : null;
            e.borderColor = m.borderColor < 0 ? null : m.borderColor;
            e.opacity = m.opacity < 0 ? null : m.opacity;
            Config.data.modules.put(m.id, e);
        }
        Config.save();
    }

    /** Stack the visible modules of each anchor and draw them. */
    public static void render(GuiGraphicsExtractor g, Minecraft mc) {
        float scale = Config.clamp(Config.data.hudScale, 70, 150) / 100.0f;
        g.pose().pushMatrix();
        try {
            g.pose().scale(scale, scale);
            int sw = Math.round(g.guiWidth() / scale);
            int sh = Math.round(g.guiHeight() / scale);
            Map<Module, HudPosition> positions = layoutPositions(mc, sw, sh);
            for (Map.Entry<Module, HudPosition> entry : positions.entrySet()) {
                Module module = entry.getKey();
                HudPosition position = entry.getValue();
                g.pose().pushMatrix();
                try {
                    g.pose().translate(position.x(), position.y());
                    float moduleScale = Config.clamp(module.scalePercent, 70, 150) / 100.0f;
                    g.pose().scale(moduleScale, moduleScale);
                    module.render(g, mc, 0, 0);
                } finally {
                    g.pose().popMatrix();
                }
            }
        } finally {
            g.pose().popMatrix();
        }
    }

    /** Returns module bounds in unscaled GUI coordinates for the editor and renderer. */
    public static Map<Module, HudPosition> layoutPositions(Minecraft mc, int sw, int sh) {
        Map<Module, HudPosition> positions = new LinkedHashMap<>();
        Module crosshair = get("crosshair");
        if (crosshair.enabled && crosshair.visible(mc)) {
            positions.put(crosshair, new HudPosition(sw / 2, sh / 2, 0, 0));
        }
        for (Anchor a : Anchor.values()) {
            List<Module> stack = new ArrayList<>();
            int total = 0;
            for (Module m : MODULES) {
                if (!m.id.equals("crosshair") && m.enabled && !m.hasCustomPosition && m.anchor == a && m.visible(mc)) {
                    stack.add(m);
                    total += scaledHeight(m, mc);
                }
            }
            if (stack.isEmpty()) {
                continue;
            }
            total += GAP * (stack.size() - 1);

            int y = switch (a.vertical) {
                case 0 -> MARGIN;
                case 1 -> (sh - total) / 2;
                default -> sh - MARGIN - total;
            };

            for (Module m : stack) {
                int w = scaledWidth(m, mc);
                int h = scaledHeight(m, mc);
                int x = a.right ? sw - MARGIN - w : MARGIN;
                positions.put(m, new HudPosition(x, y, w, h));
                y += h + GAP;
            }
        }

        for (Module m : MODULES) {
            if (m.id.equals("crosshair") || !m.enabled || !m.visible(mc) || !m.hasCustomPosition) continue;
            int w = scaledWidth(m, mc);
            int h = scaledHeight(m, mc);
            int x = Config.clamp(m.customX, 0, Math.max(0, sw - w));
            int y = Config.clamp(m.customY, 0, Math.max(0, sh - h));
            positions.put(m, new HudPosition(x, y, w, h));
        }
        return positions;
    }

    private static int scaledWidth(Module module, Minecraft mc) {
        return Math.round(module.effectiveWidth(mc) * Config.clamp(module.scalePercent, 70, 150) / 100.0f);
    }

    private static int scaledHeight(Module module, Minecraft mc) {
        return Math.round(module.effectiveHeight(mc) * Config.clamp(module.scalePercent, 70, 150) / 100.0f);
    }

    private ModuleManager() {
    }
}
