package dev.pvpclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Persistent settings, stored in config/aleksa04_1.json. */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int[] ACCENTS = {
            0xFF67C8FF, 0xFFB28AFF, 0xFF5ED6B3, 0xFFFFB86B,
            0xFFFF79A8, 0xFF55D6E8, 0xFFFF765E, 0xFFB8E66B
    };
    private static final String[] THEME_NAMES = {"Arctic", "Violet", "Mint", "Solar", "Rose", "Ocean", "Ember", "Lime"};

    public static final class Entry {
        public Boolean enabled;
        public String anchor;
        public Integer keyCode;
        public Integer color;
        public Integer scale;
        public Integer x;
        public Integer y;
        public Integer customWidth;
        public Integer customHeight;
        public Integer padding;
        public Integer borderRadius;
        public Integer backgroundColor;
        public Boolean showBorder;
        public Integer borderColor;
        public Integer opacity;
    }

    public static final class Data {
        /** FOV used while the zoom key is held (vanilla FOV slider range is 30-110). */
        public Integer zoomFov = 30;
        public Integer menuKeyCode = 344;
        public Integer zoomKeyCode = InputConstants.KEY_C;
        public Integer hudOpacity = 42;
        public Integer hudScale = 100;
        public Integer menuOpacity = 42;
        public Integer menuOverlayOpacity = 34;
        public Integer menuWidth = 920;
        public Integer menuHeight = 640;
        public Integer menuAccentColor = -1;
        public Integer menuPanelColor = -1;
        public Integer menuBorderColor = -1;
        public Integer menuTextColor = -1;
        /** 0 = low effects, 1 = maximum FPS. */
        public Integer performanceMode = 0;
        public Integer theme = 0;
        public Integer crosshairSize = 5;
        public Integer crosshairGap = 3;
        public Integer crosshairShape = 0;
        /** 0 = normalized ten-heart count, 1 = percentage, 2 = compact bar. */
        public Integer healthFormat = 0;
        /** 0 = normalized ten-food count, 1 = percentage, 2 = compact bar. */
        public Integer hungerFormat = 0;
        /** When true, show Minecraft's native health row instead of the custom widget. */
        public Boolean vanillaHealthBar = false;
        /** When true, show Minecraft's native food row instead of the custom widget. */
        public Boolean vanillaFoodBar = false;
        public Boolean dynamicCrosshair = true;
        public Boolean chroma = false;
        public Boolean compactHud = true;
        public Boolean gridSnap = false;
        public Map<String, Entry> modules = new LinkedHashMap<>();

        public int accentColor() {
            if (Boolean.TRUE.equals(chroma)) {
                float hue = (System.nanoTime() % 5_000_000_000L) / 5_000_000_000.0f;
                return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 0.82f, 1.0f) & 0x00FFFFFF);
            }
            if (menuAccentColor != null && menuAccentColor >= 0) {
                return 0xFF000000 | (menuAccentColor & 0x00FFFFFF);
            }
            return ACCENTS[Math.floorMod(theme == null ? 0 : theme, ACCENTS.length)];
        }

        public String themeName() {
            return THEME_NAMES[Math.floorMod(theme == null ? 0 : theme, THEME_NAMES.length)];
        }

        public int themeCount() {
            return THEME_NAMES.length;
        }
    }

    public static Data data = new Data();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("aleksa04_1.json");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(p)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            if (loaded != null) {
                Data defaults = new Data();
                if (loaded.zoomFov == null) loaded.zoomFov = defaults.zoomFov;
                if (loaded.menuKeyCode == null) loaded.menuKeyCode = defaults.menuKeyCode;
                if (loaded.zoomKeyCode == null) loaded.zoomKeyCode = defaults.zoomKeyCode;
                if (loaded.hudOpacity == null) loaded.hudOpacity = defaults.hudOpacity;
                if (loaded.hudScale == null) loaded.hudScale = defaults.hudScale;
                if (loaded.menuOpacity == null) loaded.menuOpacity = defaults.menuOpacity;
                if (loaded.menuOverlayOpacity == null) loaded.menuOverlayOpacity = defaults.menuOverlayOpacity;
                if (loaded.menuWidth == null) loaded.menuWidth = defaults.menuWidth;
                if (loaded.menuHeight == null) loaded.menuHeight = defaults.menuHeight;
                if (loaded.menuAccentColor == null) loaded.menuAccentColor = defaults.menuAccentColor;
                if (loaded.menuPanelColor == null) loaded.menuPanelColor = defaults.menuPanelColor;
                if (loaded.menuBorderColor == null) loaded.menuBorderColor = defaults.menuBorderColor;
                if (loaded.menuTextColor == null) loaded.menuTextColor = defaults.menuTextColor;
                if (loaded.performanceMode == null) loaded.performanceMode = defaults.performanceMode;
                if (loaded.theme == null) loaded.theme = defaults.theme;
                if (loaded.crosshairSize == null) loaded.crosshairSize = defaults.crosshairSize;
                if (loaded.crosshairGap == null) loaded.crosshairGap = defaults.crosshairGap;
                if (loaded.crosshairShape == null) loaded.crosshairShape = defaults.crosshairShape;
                if (loaded.healthFormat == null) loaded.healthFormat = defaults.healthFormat;
                if (loaded.hungerFormat == null) loaded.hungerFormat = defaults.hungerFormat;
                if (loaded.vanillaHealthBar == null) loaded.vanillaHealthBar = defaults.vanillaHealthBar;
                if (loaded.vanillaFoodBar == null) loaded.vanillaFoodBar = defaults.vanillaFoodBar;
                if (loaded.dynamicCrosshair == null) loaded.dynamicCrosshair = defaults.dynamicCrosshair;
                if (loaded.chroma == null) loaded.chroma = defaults.chroma;
                if (loaded.compactHud == null) loaded.compactHud = defaults.compactHud;
                if (loaded.gridSnap == null) loaded.gridSnap = defaults.gridSnap;
                loaded.zoomFov = clamp(loaded.zoomFov, 30, 110);
                loaded.hudOpacity = clamp(loaded.hudOpacity, 0, 100);
                loaded.hudScale = clamp(loaded.hudScale, 70, 150);
                loaded.menuOpacity = clamp(loaded.menuOpacity, 30, 100);
                loaded.menuOverlayOpacity = clamp(loaded.menuOverlayOpacity, 0, 80);
                loaded.menuWidth = clamp(loaded.menuWidth, 700, 1200);
                loaded.menuHeight = clamp(loaded.menuHeight, 540, 800);
                loaded.menuAccentColor = normalizeColor(loaded.menuAccentColor);
                loaded.menuPanelColor = normalizeColor(loaded.menuPanelColor);
                loaded.menuBorderColor = normalizeColor(loaded.menuBorderColor);
                loaded.menuTextColor = normalizeColor(loaded.menuTextColor);
                loaded.performanceMode = Math.floorMod(loaded.performanceMode, 2);
                loaded.theme = Math.floorMod(loaded.theme, ACCENTS.length);
                loaded.crosshairSize = clamp(loaded.crosshairSize, 2, 12);
                loaded.crosshairGap = clamp(loaded.crosshairGap, 0, 12);
                loaded.crosshairShape = Math.floorMod(loaded.crosshairShape, 3);
                loaded.healthFormat = Math.floorMod(loaded.healthFormat, 3);
                loaded.hungerFormat = Math.floorMod(loaded.hungerFormat, 3);
                if (loaded.modules == null) loaded.modules = new LinkedHashMap<>();
                data = loaded;
            }
        } catch (Exception e) {
            System.err.println("[aleksa04_1] Could not read config, using defaults: " + e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(path().getParent());
            try (Writer writer = Files.newBufferedWriter(path())) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[aleksa04_1] Could not save config: " + e);
        }
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int normalizeColor(int color) {
        return color < 0 ? -1 : color & 0x00FFFFFF;
    }

    private Config() {
    }
}
