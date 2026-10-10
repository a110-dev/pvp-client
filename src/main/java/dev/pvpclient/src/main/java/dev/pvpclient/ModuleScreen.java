package dev.pvpclient;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import dev.pvpclient.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Lunar-inspired animated control center for modules, search, color, and HUD layout. */
public final class ModuleScreen extends Screen {
    private static final int DEFAULT_PANEL_RGB = 0x00111318;
    private static final int DEFAULT_BORDER = 0xFF2B2E38;
    private static final int[] COLOR_SWATCHES = {
            0xFF67C8FF, 0xFFB28AFF, 0xFF5ED6B3, 0xFFFFB86B,
            0xFFFF79A8, 0xFF55D6E8, 0xFFFF765E, 0xFFB8E66B
    };
    private static int PANEL_RGB = DEFAULT_PANEL_RGB;
    private static int HEADER_RGB = 0x00181A20;
    private static int CARD_RGB = 0x00191C23;
    private static int CARD_HOVER_RGB = 0x00242832;
    private static int BORDER = DEFAULT_BORDER;
    private static int MUTED = 0xFF969BA8;
    private static int WHITE = 0xFFF1F2F5;
    private static final String[] TABS = {"ALL", "COMBAT", "HUD", "RENDER", "MOVEMENT", "UTILITY", "PERFORMANCE", "STYLE", "SETTINGS"};
    private static final String[] TAB_LABELS = {"All", "Combat", "HUD", "Render", "Move", "Other", "FPS", "Style", "Settings"};

    private final Screen parent;
    private final long openedAt = System.nanoTime();
    private final Map<String, Float> hoverAmounts = new HashMap<>();
    private String selectedTab = "ALL";
    private String selectedModuleId;
    private String bindingTarget;
    private String activeSlider;
    private int styleColorTarget;
    private String searchText = "";
    private boolean searchFocused;
    private int scrollOffset;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public ModuleScreen(Screen parent) {
        super(Component.literal("PVP CLIENT - MODS"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        syncPalette();

        panelWidth = Math.min(Config.data.menuWidth, Math.max(320, width - 32));
        panelHeight = Math.min(Config.data.menuHeight, Math.max(280, height - 32));
        panelX = (width - panelWidth) / 2;
        float t = Math.min(1.0f, (System.nanoTime() - openedAt) / 360_000_000.0f);
        float eased = 1.0f - (float) Math.pow(1.0f - t, 3.0f);
        int targetY = (height - panelHeight) / 2;
        panelY = targetY - Math.round((1.0f - eased) * 34.0f);

        int backdropAlpha = Config.clamp(Config.data.menuOverlayOpacity, 0, 80) * 255 / 100;
        g.fill(0, 0, width, height, backdropAlpha << 24);
        g.fill(panelX + 4, panelY + 7, panelX + panelWidth + 4, panelY + panelHeight + 7, 0x60000000);
        g.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, surface(PANEL_RGB, 0));
        outline(g, panelX, panelY, panelWidth, panelHeight, BORDER);
        g.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + 64, surface(HEADER_RGB, 24));
        g.fill(panelX + 1, panelY + 64, panelX + panelWidth - 1, panelY + 65, BORDER);

        drawHeader(g, eased);
        drawTabs(g, mouseX, mouseY);
        if (selectedModuleId != null) {
            drawModuleSettings(g, mouseX, mouseY, ModuleManager.get(selectedModuleId));
        } else if (selectedTab.equals("SETTINGS")) {
            drawSettings(g, mouseX, mouseY);
        } else if (selectedTab.equals("PERFORMANCE")) {
            drawPerformanceSettings(g, mouseX, mouseY);
        } else if (selectedTab.equals("STYLE")) {
            drawAppearanceSettings(g, mouseX, mouseY);
        } else {
            drawModules(g, mouseX, mouseY);
        }
        drawFooter(g, mouseX, mouseY);
        if (bindingTarget != null) drawBindPrompt(g);
    }

    private void drawHeader(GuiGraphicsExtractor g, float eased) {
        int accent = Config.data.accentColor();
        g.fill(panelX + 20, panelY + 16, panelX + 52, panelY + 48, accent);
        g.fill(panelX + 24, panelY + 20, panelX + 48, panelY + 44, surface(HEADER_RGB, 24));
        g.centeredText(font, "P", panelX + 36, panelY + 28, WHITE);
        g.text(font, Component.literal("PVP CLIENT - MODS").withStyle(style -> style.withBold(true)),
                panelX + 64, panelY + 17, WHITE);
        g.text(font, SolarCredits.BRAND + "  |  " + SolarCredits.CREDIT,
                panelX + 64, panelY + 38, MUTED);
        g.text(font, SolarCredits.runtimeLine(), panelX + 64, panelY + 52, MUTED);

        int enabled = 0;
        for (Module module : ModuleManager.all()) if (module.enabled) enabled++;
        String count = String.format(Locale.ROOT, "%02d ACTIVE", enabled);
        int countWidth = font.width(count);
        int right = panelX + panelWidth - 22;
        g.fill(right - countWidth - 18, panelY + 20, right, panelY + 44, 0xFF22252D);
        g.text(font, count, right - countWidth - 9, panelY + 28, accent);

        int sweepRange = Math.max(1, panelWidth - 40);
        int sweep = (int) ((System.nanoTime() / 2_000_000L) % sweepRange);
        int sweepX = panelX + 18 + sweep;
        g.fill(sweepX, panelY + 62, Math.min(panelX + panelWidth - 18, sweepX + 70), panelY + 64,
                withAlpha(accent & 0x00FFFFFF, Math.round(220 * eased)));
    }

    private void drawTabs(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y = panelY + 77;
        int x = panelX + 18;
        for (int i = 0; i < TABS.length; i++) {
            int tabWidth = tabWidth(i);
            drawButton(g, mouseX, mouseY, x, y, tabWidth, 29, TAB_LABELS[i],
                    selectedTab.equals(TABS[i]) ? Config.data.accentColor() : cardColor(), true);
            if (selectedTab.equals(TABS[i])) g.fill(x + 6, y + 27, x + tabWidth - 6, y + 29, WHITE);
            x += tabWidth + 5;
        }

        int searchX = panelX + panelWidth - 214;
        int searchY = panelY + 77;
        int searchW = 196;
        g.fill(searchX, searchY, searchX + searchW, searchY + 29,
                searchFocused ? cardHoverColor() : cardColor());
        outline(g, searchX, searchY, searchW, 29, searchFocused ? Config.data.accentColor() : BORDER);
        String shown = searchText.isEmpty() ? "Search modules..." : searchText;
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) shown += "|";
        g.text(font, shown, searchX + 10, searchY + 10,
                searchText.isEmpty() && !searchFocused ? MUTED : WHITE);
    }

    private void drawModules(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int contentX = panelX + 20;
        int contentWidth = panelWidth - 40;
        int contentTop = panelY + 125;
        String title = selectedTab.equals("ALL") ? "Module Library" : selectedTab.substring(0, 1) + selectedTab.substring(1).toLowerCase(Locale.ROOT) + " Modules";
        g.text(font, title, contentX, contentTop, WHITE);
        g.text(font, "Search, customize, and drag your client HUD into place.", contentX, contentTop + 19, MUTED);

        List<Module> modules = filteredModules();
        int rowsY = contentTop + 43;
        int rowStep = 60;
        int visibleRows = Math.max(1, (panelY + panelHeight - 57 - rowsY) / rowStep);
        scrollOffset = Config.clamp(scrollOffset, 0, Math.max(0, modules.size() - visibleRows));
        for (int row = 0; row < visibleRows; row++) {
            int index = scrollOffset + row;
            if (index >= modules.size()) break;
            Module module = modules.get(index);
            int y = rowsY + row * rowStep;
            boolean hover = inside(mouseX, mouseY, contentX, y, contentWidth, 53);
            float hoverT = lerpHover("row-" + module.id, hover);
            g.fill(contentX, y, contentX + contentWidth, y + 53,
                    mix(cardColor(), cardHoverColor(), hoverT));
            outline(g, contentX, y, contentWidth, 53, BORDER);
            if (module.enabled) g.fill(contentX, y + 6, contentX + 3, y + 47, module.accentColor());
            g.text(font, module.name, contentX + 14, y + 9, WHITE);
            g.text(font, module.description(), contentX + 14, y + 29, MUTED);

            int toggleX = contentX + contentWidth - 294;
            int bindX = toggleX + 74;
            int positionX = bindX + 88;
            int gearX = positionX + 88;
            drawButton(g, mouseX, mouseY, toggleX, y + 12, 68, 29,
                    module.enabled ? "ENABLED" : "DISABLED", module.enabled ? 0xFF244A39 : 0xFF3B2930, true);
            drawButton(g, mouseX, mouseY, bindX, y + 12, 82, 29,
                    bindingTarget != null && bindingTarget.equals(module.id) ? "PRESS KEY" : bindLabel(module), 0xFF272B34, true);
            drawButton(g, mouseX, mouseY, positionX, y + 12, 82, 29,
                    "POS  " + anchorLabel(module.anchor), 0xFF272B34, true);
            drawButton(g, mouseX, mouseY, gearX, y + 12, 32, 29, "⚙", 0xFF272B34, true);
        }

        if (modules.size() > visibleRows) {
            int trackX = contentX + contentWidth - 2;
            int trackTop = rowsY;
            int trackBottom = rowsY + visibleRows * rowStep - 7;
            int trackHeight = Math.max(1, trackBottom - trackTop);
            int thumbHeight = Math.max(22, trackHeight * visibleRows / modules.size());
            int maxScroll = modules.size() - visibleRows;
            int thumbY = trackTop + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
            g.fill(trackX, trackTop, trackX + 2, trackBottom, BORDER);
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, Config.data.accentColor());
        }
        if (modules.isEmpty()) g.text(font, "No modules found. Try a different search.", contentX, rowsY + 8, MUTED);
    }

    private void drawSettings(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int top = panelY + 125;
        g.text(font, "Client Settings", x, top, WHITE);
        g.text(font, "Personalize the menu, HUD, crosshair, and global keybinds.", x, top + 19, MUTED);
        int rowY = top + 43;
        int step = settingsStep();
        int rowHeight = step - 4;

        String themeLabel = Config.data.menuAccentColor >= 0 && !Boolean.TRUE.equals(Config.data.chroma)
                ? "CUSTOM" : Config.data.themeName();
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Accent Theme", "Eight built-in palettes", panelButton(x, w, "theme"), themeLabel);
        rowY += step;
        drawSliderRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "HUD Background Opacity", "Lower values show more of the game", Config.data.hudOpacity, 0, 100, "%");
        rowY += step;
        drawSliderRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Menu Transparency", "Show more of the game behind the menu", Config.data.menuOpacity, 30, 100, "%");
        rowY += step;
        drawSliderRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "HUD Scale", "Resize every module", Config.data.hudScale, 70, 150, "%");
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Zoom FOV", "Lower values zoom in further", panelButton(x, w, "zoom"), "−  " + Config.data.zoomFov + "°  +");
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Crosshair", "Size and bloom behavior are adjustable", panelButton(x, w, "crosshair"), Config.data.crosshairSize + " px");
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Menu Key", "Press a key to open and close the menu", panelButton(x, w, "$menu"),
                bindingTarget != null && bindingTarget.equals("$menu") ? "PRESS KEY" : KeybindManager.label(KeybindManager.menuKey));
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Zoom Key", "Hold to zoom smoothly", panelButton(x, w, "$zoom"),
                bindingTarget != null && bindingTarget.equals("$zoom") ? "PRESS KEY" : KeybindManager.label(KeybindManager.zoomKey));
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Chroma Accent", "Animate the accent through a rainbow", panelButton(x, w, "chroma"),
                Boolean.TRUE.equals(Config.data.chroma) ? "ON" : "OFF");
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "HUD Layout", "Compact tiles or expanded cards", panelButton(x, w, "compact"),
                Boolean.TRUE.equals(Config.data.compactHud) ? "COMPACT" : "EXPANDED");
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "Attack Bloom", "Crosshair opens briefly on a click", panelButton(x, w, "bloom"),
                Boolean.TRUE.equals(Config.data.dynamicCrosshair) ? "ON" : "OFF");
        rowY += step;
        settingsRow(g, mouseX, mouseY, x, rowY, w, rowHeight, "HUD Grid Snap", "Optional 4-pixel alignment while dragging", panelButton(x, w, "gridSnap"),
                Boolean.TRUE.equals(Config.data.gridSnap) ? "ON" : "OFF");
    }

    private void drawAppearanceSettings(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int top = panelY + 125;
        g.text(font, "Menu Appearance", x, top, WHITE);
        g.text(font, "Edit the menu palette, size, and how strongly it dims the game.", x, top + 19, MUTED);

        int y = top + 43;
        int rowHeight = settingsStep() - 4;
        g.fill(x, y, x + w, y + rowHeight, cardColor());
        outline(g, x, y, w, rowHeight, BORDER);
        String[] targets = {"ACCENT", "PANEL", "BORDER", "TEXT"};
        int resetWidth = 92;
        int gap = 5;
        int targetWidth = (w - resetWidth - gap * 4 - 10) / targets.length;
        int targetX = x + 6;
        for (int i = 0; i < targets.length; i++) {
            drawButton(g, mouseX, mouseY, targetX, y + 4, targetWidth, rowHeight - 8, targets[i],
                    styleColorTarget == i ? Config.data.accentColor() : 0xFF272B34, true);
            targetX += targetWidth + gap;
        }
        drawButton(g, mouseX, mouseY, x + w - resetWidth - 6, y + 4, resetWidth, rowHeight - 8,
                "RESET", 0xFF272B34, true);

        int color = styleColor();
        int step = settingsStep();
        int sliderHeight = step - 4;
        y += step;
        g.fill(x, y, x + w, y + sliderHeight, cardColor());
        outline(g, x, y, w, sliderHeight, BORDER);
        g.text(font, "Quick Colors", x + 12, y + (sliderHeight - font.lineHeight) / 2, WHITE);
        int swatchSize = Math.max(14, step - 12);
        int swatchGap = 5;
        int paletteWidth = COLOR_SWATCHES.length * swatchSize + (COLOR_SWATCHES.length - 1) * swatchGap;
        int swatchX = x + w - paletteWidth - 12;
        int swatchY = y + (sliderHeight - swatchSize) / 2;
        for (int i = 0; i < COLOR_SWATCHES.length; i++) {
            int sx = swatchX + i * (swatchSize + swatchGap);
            g.fill(sx, swatchY, sx + swatchSize, swatchY + swatchSize, COLOR_SWATCHES[i]);
            boolean selected = (color & 0x00FFFFFF) == (COLOR_SWATCHES[i] & 0x00FFFFFF);
            outline(g, sx - 1, swatchY - 1, swatchSize + 2, swatchSize + 2,
                    selected ? WHITE : (inside(mouseX, mouseY, sx, swatchY, swatchSize, swatchSize)
                            ? Config.data.accentColor() : BORDER));
        }

        y += step;
        drawSliderRow(g, mouseX, mouseY, x, y, w, sliderHeight, "Red", "Selected menu color", (color >> 16) & 255, 0, 255, "");
        y += step;
        drawSliderRow(g, mouseX, mouseY, x, y, w, sliderHeight, "Green", "Selected menu color", (color >> 8) & 255, 0, 255, "");
        y += step;
        drawSliderRow(g, mouseX, mouseY, x, y, w, sliderHeight, "Blue", "Selected menu color", color & 255, 0, 255, "");
        y += step;
        drawSliderRow(g, mouseX, mouseY, x, y, w, sliderHeight, "Menu Width", "Panel width", Config.data.menuWidth, 700, 1200, " px");
        y += step;
        drawSliderRow(g, mouseX, mouseY, x, y, w, sliderHeight, "Menu Height", "Panel height", Config.data.menuHeight, 540, 800, " px");
        y += step;
        drawSliderRow(g, mouseX, mouseY, x, y, w, sliderHeight, "Backdrop Dim", "Dark tint behind the menu", Config.data.menuOverlayOpacity, 0, 80, "%");
    }

    private void drawPerformanceSettings(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int top = panelY + 125;
        g.text(font, "Performance Center", x, top, WHITE);
        g.text(font, "Tune the built-in profile or pair this client with compatible Fabric optimization mods.", x, top + 19, MUTED);

        int y = top + 48;
        int h = 58;
        Module profile = ModuleManager.get("performance");
        g.fill(x, y, x + w, y + h, inside(mouseX, mouseY, x, y, w, h) ? cardHoverColor() : cardColor());
        outline(g, x, y, w, h, BORDER);
        g.fill(x, y + 5, x + 3, y + h - 5, profile.enabled ? Config.data.accentColor() : BORDER);
        String modeName = Config.data.performanceMode == 1 ? "MAX FPS" : "LOW EFFECTS";
        g.text(font, "Built-in FPS Profile", x + 13, y + 9, WHITE);
        g.text(font, "Reversible render, particle, cloud, and effect tuning.", x + 13, y + 31, MUTED);
        drawButton(g, mouseX, mouseY, x + w - 322, y + 15, 152, 28, modeName, 0xFF272B34, true);
        drawButton(g, mouseX, mouseY, x + w - 160, y + 15, 146, 28,
                profile.enabled ? "ACTIVE  ·  RESTORE" : "APPLY PROFILE", profile.enabled ? 0xFF244A39 : 0xFF272B34, true);

        int gapX = 10;
        int colW = (w - gapX) / 2;
        int gridY = y + 68;
        drawPerformanceAddon(g, mouseX, mouseY, x, gridY, colW, h, "Sodium",
                "GPU renderer optimization.", "sodium");
        drawPerformanceAddon(g, mouseX, mouseY, x + colW + gapX, gridY, colW, h, "Lithium",
                "Game logic and ticking.", "lithium");
        gridY += 68;
        drawPerformanceAddon(g, mouseX, mouseY, x, gridY, colW, h, "Entity Culling",
                "Skip hidden entities.", "entityculling");
        drawPerformanceAddon(g, mouseX, mouseY, x + colW + gapX, gridY, colW, h, "ImmediatelyFast",
                "Batch immediate-mode draws.", "immediatelyfast");
        gridY += 68;
        drawPerformanceAddon(g, mouseX, mouseY, x, gridY, colW, h, "FerriteCore",
                "Reduce memory use.", "ferritecore");
        g.text(font, "Install optional Fabric add-ons in the instance's mods folder; PvPClient detects but does not bundle them.",
                x + 2, gridY + h + 12, MUTED);
    }

    private void drawPerformanceAddon(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h,
                                      String title, String subtitle, String modId) {
        boolean loaded = FabricLoader.getInstance().isModLoaded(modId);
        g.fill(x, y, x + w, y + h, inside(mouseX, mouseY, x, y, w, h) ? cardHoverColor() : cardColor());
        outline(g, x, y, w, h, BORDER);
        g.text(font, title, x + 13, y + 9, WHITE);
        g.text(font, subtitle, x + 13, y + 31, MUTED);
        String state = loaded ? "DETECTED" : "NOT INSTALLED";
        int stateColor = loaded ? 0xFF78D9A1 : 0xFFFFB86B;
        int badgeWidth = font.width(state) + 20;
        int badgeX = x + w - badgeWidth - 14;
        g.fill(badgeX, y + 15, badgeX + badgeWidth, y + 43, surface(0x00191C23, 58));
        outline(g, badgeX, y + 15, badgeWidth, 28, BORDER);
        g.centeredText(font, state, badgeX + badgeWidth / 2, y + 23, stateColor);
    }

    private void drawModuleSettings(GuiGraphicsExtractor g, int mouseX, int mouseY, Module module) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int top = panelY + 125;
        drawButton(g, mouseX, mouseY, x, top, 76, 28, "← BACK", 0xFF272B34, true);
        g.text(font, module.name + " Settings", x + 92, top + 4, WHITE);
        g.text(font, module.description(), x + 92, top + 22, MUTED);

        int sliderX = x + Math.min(300, Math.max(220, w / 3));
        int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
        int rowY = top + 56;
        int color = module.customColor < 0 ? Config.data.accentColor() : module.customColor;
        int[] values = {(color >> 16) & 255, (color >> 8) & 255, color & 255};
        String[] channels = {"Red", "Green", "Blue"};
        for (int i = 0; i < 3; i++) {
            drawLabeledSlider(g, mouseX, mouseY, x, rowY, w, channels[i] + " channel", values[i], 0, 255,
                    sliderX, sliderWidth, new int[]{0xFFFF6868, 0xFF66DB91, 0xFF6EA6FF}[i]);
            rowY += 48;
        }

        drawLabeledSlider(g, mouseX, mouseY, x, rowY, w, "Module Scale", module.scalePercent, 70, 150,
                sliderX, sliderWidth, Config.data.accentColor());
        drawButton(g, mouseX, mouseY, x + w - 138, rowY + 5, 126, 27, "RESET COLOR", 0xFF272B34, true);
        rowY += 50;

        // Custom Size
        g.fill(x, rowY, x + w, rowY + 39, cardColor());
        outline(g, x, rowY, w, 39, BORDER);
        g.text(font, "Custom Size", x + 12, rowY + 4, WHITE);
        String sizeLabel = module.customWidth > 0 && module.customHeight > 0
                ? module.customWidth + "x" + module.customHeight : "AUTO";
        g.text(font, "Custom width and height for this module", x + 12, rowY + 20, MUTED);
        drawButton(g, mouseX, mouseY, x + w - 208, rowY + 6, 96, 27, "WIDTH +", 0xFF272B34, true);
        drawButton(g, mouseX, mouseY, x + w - 104, rowY + 6, 92, 27, "HEIGHT +", 0xFF272B34, true);
        rowY += 50;

        // Custom Size sliders
        int currentWidth = module.customWidth > 0 ? module.customWidth : module.width(Minecraft.getInstance());
        int currentHeight = module.customHeight > 0 ? module.customHeight : module.height(Minecraft.getInstance());
        drawLabeledSlider(g, mouseX, mouseY, x, rowY, w, "Width", currentWidth, 10, 400,
                sliderX, sliderWidth, Config.data.accentColor());
        rowY += 48;
        drawLabeledSlider(g, mouseX, mouseY, x, rowY, w, "Height", currentHeight, 10, 400,
                sliderX, sliderWidth, Config.data.accentColor());
        rowY += 50;

        // Padding
        drawLabeledSlider(g, mouseX, mouseY, x, rowY, w, "Padding", module.padding, 0, 20,
                sliderX, sliderWidth, Config.data.accentColor());
        rowY += 50;

        // Border Radius
        drawLabeledSlider(g, mouseX, mouseY, x, rowY, w, "Border Radius", module.borderRadius, 0, 16,
                sliderX, sliderWidth, Config.data.accentColor());
        rowY += 50;

        // Opacity
        int currentOpacity = module.opacity >= 0 ? module.opacity : Config.data.hudOpacity;
        drawLabeledSlider(g, mouseX, mouseY, x, rowY, w, "Opacity", currentOpacity, 0, 100,
                sliderX, sliderWidth, Config.data.accentColor());
        rowY += 50;

        // Border
        g.fill(x, rowY, x + w, rowY + 39, cardColor());
        outline(g, x, rowY, w, 39, BORDER);
        g.text(font, "Border", x + 12, rowY + 4, WHITE);
        g.text(font, "Show border around this module", x + 12, rowY + 20, MUTED);
        drawButton(g, mouseX, mouseY, x + w - 174, rowY + 6, 162, 27,
                module.showBorder ? "BORDER: ON" : "BORDER: OFF", module.showBorder ? 0xFF315744 : 0xFF272B34, true);
        rowY += 50;

        g.fill(x, rowY, x + w, rowY + 39, cardColor());
        outline(g, x, rowY, w, 39, BORDER);
        g.text(font, "HUD Position", x + 12, rowY + 4, WHITE);
        g.text(font, "Choose a starting corner or use Edit HUD to drag this module.", x + 12, rowY + 20, MUTED);
        drawButton(g, mouseX, mouseY, x + w - 208, rowY + 6, 96, 27, "NEXT POS", 0xFF272B34, true);
        drawButton(g, mouseX, mouseY, x + w - 104, rowY + 6, 92, 27, "RESET", 0xFF272B34, true);
        rowY += 50;
        if (module.id.equals("health") || module.id.equals("hunger")) {
            int format = module.id.equals("health") ? Config.data.healthFormat : Config.data.hungerFormat;
            String[] formats = {"10-UNIT", "PERCENT", "BAR"};
            g.fill(x, rowY, x + w, rowY + 39, cardColor());
            outline(g, x, rowY, w, 39, BORDER);
            g.text(font, "Vital Display", x + 12, rowY + 4, WHITE);
            g.text(font, "Ten-unit count, percentage, or compact bar", x + 12, rowY + 20, MUTED);
            drawButton(g, mouseX, mouseY, x + w - 174, rowY + 6, 162, 27,
                    formats[Math.floorMod(format, formats.length)], 0xFF272B34, true);
            rowY += 48;
            boolean vanilla = module.id.equals("health")
                    ? Boolean.TRUE.equals(Config.data.vanillaHealthBar)
                    : Boolean.TRUE.equals(Config.data.vanillaFoodBar);
            g.fill(x, rowY, x + w, rowY + 39, cardColor());
            outline(g, x, rowY, w, 39, BORDER);
            g.text(font, "Vanilla Bar", x + 12, rowY + 4, WHITE);
            g.text(font, "Use Minecraft's original " + (module.id.equals("health") ? "heart" : "hunger") + " row",
                    x + 12, rowY + 20, MUTED);
            drawButton(g, mouseX, mouseY, x + w - 174, rowY + 6, 162, 27,
                    vanilla ? "VANILLA: ON" : "VANILLA: OFF", vanilla ? 0xFF315744 : 0xFF272B34, true);
            rowY += 48;
        }
        if (module.id.equals("crosshair")) {
            g.fill(x, rowY, x + w, rowY + 39, cardColor());
            outline(g, x, rowY, w, 39, BORDER);
            g.text(font, "Crosshair Shape", x + 12, rowY + 4, WHITE);
            g.text(font, "Cross, dot, or ring", x + 12, rowY + 20, MUTED);
            drawButton(g, mouseX, mouseY, x + w - 174, rowY + 6, 162, 27, "CHANGE SHAPE", 0xFF272B34, true);
            rowY += 48;
            g.fill(x, rowY, x + w, rowY + 39, cardColor());
            outline(g, x, rowY, w, 39, BORDER);
            g.text(font, "Crosshair Gap", x + 12, rowY + 4, WHITE);
            g.text(font, "Space between the center and lines  •  " + Config.data.crosshairGap + " px", x + 12, rowY + 20, MUTED);
            drawButton(g, mouseX, mouseY, x + w - 174, rowY + 6, 162, 27, "INCREASE GAP", 0xFF272B34, true);
        }
    }

    private void drawSliderRow(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w,
                               int h, String title, String subtitle, int value, int min, int max, String suffix) {
        g.fill(x, y, x + w, y + h, inside(mouseX, mouseY, x, y, w, h) ? cardHoverColor() : cardColor());
        outline(g, x, y, w, h, BORDER);
        g.text(font, title, x + 12, y + 4, WHITE);
        g.text(font, subtitle, x + 12, y + 18, MUTED);
        int sliderX = x + w - 208;
        drawSlider(g, sliderX, y + 17, 140, value, min, max, Config.data.accentColor());
        g.text(font, value + suffix, x + w - 56, y + 14, WHITE);
    }

    private void drawLabeledSlider(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w,
                                   String label, int value, int min, int max, int sx, int sw, int color) {
        g.fill(x, y, x + w, y + 40, inside(mouseX, mouseY, x, y, w, 40) ? cardHoverColor() : cardColor());
        outline(g, x, y, w, 40, BORDER);
        g.text(font, label, x + 12, y + 14, WHITE);
        drawSlider(g, sx, y + 18, sw, value, min, max, color);
        g.text(font, Integer.toString(value), sx + sw + 12, y + 14, WHITE);
    }

    private void drawSlider(GuiGraphicsExtractor g, int x, int y, int width, int value, int min, int max, int color) {
        g.fill(x, y, x + width, y + 4, BORDER);
        int filled = width * (value - min) / (max - min);
        g.fill(x, y, x + filled, y + 4, color);
        g.fill(x + filled - 3, y - 3, x + filled + 3, y + 7, WHITE);
    }

    private void settingsRow(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h,
                             String title, String subtitle, int buttonX, String value) {
        g.fill(x, y, x + w, y + h, inside(mouseX, mouseY, x, y, w, h) ? cardHoverColor() : cardColor());
        outline(g, x, y, w, h, BORDER);
        g.text(font, title, x + 12, y + 3, WHITE);
        g.text(font, subtitle, x + 12, y + 17, MUTED);
        drawButton(g, mouseX, mouseY, buttonX, y + 5, x + w - buttonX - 10, h - 10, value, 0xFF272B34, true);
    }

    private int panelButton(int x, int w, String action) {
        return x + w - (action.equals("theme") ? 122 : 160);
    }

    private void drawButton(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int width, int height,
                            String label, int color, boolean center) {
        if (color == 0xFF272B34 || color == 0xFF20232A || color == 0xFF242832) {
            color = surface(color, 36);
        }
        boolean over = inside(mouseX, mouseY, x, y, width, height);
        float hover = lerpHover("button-" + x + "-" + y, over);
        int animated = mix(color, 0xFFFFFFFF, hover * 0.14f);
        g.fill(x, y, x + width, y + height, animated);
        outline(g, x, y, width, height, over ? mix(BORDER, Config.data.accentColor(), hover) : BORDER);
        int brightness = ((animated >> 16) & 255) * 3 + ((animated >> 8) & 255) * 6 + (animated & 255);
        int textColor = brightness < 900 ? WHITE : 0xFF101318;
        if (center) g.centeredText(font, label, x + width / 2, y + (height - font.lineHeight) / 2, textColor);
        else g.text(font, label, x + 8, y + (height - font.lineHeight) / 2, textColor);
    }

    private void drawFooter(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int footerY = panelY + panelHeight - 34;
        g.fill(panelX + 1, footerY - 8, panelX + panelWidth - 1, footerY - 7, BORDER);
        drawButton(g, mouseX, mouseY, panelX + 18, footerY, 106, 28, "EDIT HUD", Config.data.accentColor(), true);
        drawButton(g, mouseX, mouseY, panelX + 130, footerY, 98, 28, "GALLERY", 0xFF272B34, true);
        Minecraft mc = Minecraft.getInstance();
        boolean reconnectable = mc.getCurrentServer() != null && !mc.isLocalServer();
        drawButton(g, mouseX, mouseY, panelX + 234, footerY, 112, 28, "RECONNECT",
                reconnectable ? 0xFF272B34 : 0xFF20232A, true);
        String footer = bindingTarget == null ? "ESC CLOSE  •  SCROLL TO BROWSE" : "PRESS A KEY TO BIND  •  ESC TO CLEAR";
        g.text(font, footer, panelX + panelWidth - font.width(footer) - 20, footerY + 9, MUTED);
    }

    private void drawBindPrompt(GuiGraphicsExtractor g) {
        int w = 250;
        int x = panelX + (panelWidth - w) / 2;
        int y = panelY + panelHeight - 82;
        g.fill(x, y, x + w, y + 30, 0xFF242832);
        g.fill(x, y, x + 3, y + 30, Config.data.accentColor());
        g.centeredText(font, "PRESS A KEY  •  ESC TO CLEAR", x + w / 2, y + 9, WHITE);
    }

    private List<Module> filteredModules() {
        List<Module> result = new ArrayList<>();
        String query = searchText.toLowerCase(Locale.ROOT).trim();
        for (Module module : ModuleManager.all()) {
            if (!selectedTab.equals("ALL") && !selectedTab.equals(module.category())) continue;
            if (!query.isEmpty() && !module.name.toLowerCase(Locale.ROOT).contains(query)
                    && !module.description().toLowerCase(Locale.ROOT).contains(query)) continue;
            result.add(module);
        }
        return result;
    }

    private String bindLabel(Module module) {
        String label = KeybindManager.label(KeybindManager.bindingFor(module));
        return label.length() > 9 ? label.substring(0, 8) + "…" : label;
    }

    private int tabWidth(int index) {
        int natural = font.width(TAB_LABELS[index]) + 20;
        int searchX = panelX + panelWidth - 214;
        int available = searchX - (panelX + 18) - 12;
        int naturalWidths = 0;
        for (String label : TAB_LABELS) naturalWidths += font.width(label) + 20;
        int gapWidth = 5 * (TAB_LABELS.length - 1);
        if (naturalWidths + gapWidth <= available) return natural;
        int labelSpace = Math.max(TAB_LABELS.length * 28, available - gapWidth);
        return Math.max(28, Math.round(natural * labelSpace / (float) naturalWidths));
    }

    private int settingsStep() {
        return Math.min(43, Math.max(27, (panelHeight - 206) / 12));
    }

    private void save() {
        ModuleManager.save();
        Config.save();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Minecraft 26.3's MouseButtonInfo is one-based: left=1, middle=2, right=3.
        if (event.button() != 1) return true;
        double mouseX = event.x();
        double mouseY = event.y();
        if (bindingTarget != null) return true;

        int tabsY = panelY + 77;
        int tabX = panelX + 18;
        for (int i = 0; i < TABS.length; i++) {
            int tabWidth = tabWidth(i);
            if (inside(mouseX, mouseY, tabX, tabsY, tabWidth, 29)) {
                selectedTab = TABS[i];
                selectedModuleId = null;
                scrollOffset = 0;
                searchFocused = false;
                return true;
            }
            tabX += tabWidth + 5;
        }

        int searchX = panelX + panelWidth - 214;
        if (inside(mouseX, mouseY, searchX, panelY + 77, 196, 29)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        int editY = panelY + panelHeight - 34;
        if (inside(mouseX, mouseY, panelX + 18, editY, 106, 28)) {
            Minecraft.getInstance().gui.setScreen(new HudEditorScreen(this));
            return true;
        }
        if (inside(mouseX, mouseY, panelX + 130, editY, 98, 28)) {
            Minecraft.getInstance().gui.setScreen(new ScreenshotManagerScreen(this));
            return true;
        }
        if (inside(mouseX, mouseY, panelX + 234, editY, 112, 28)) {
            ReconnectManager.reconnect(Minecraft.getInstance(), this);
            return true;
        }

        if (selectedModuleId != null) {
            handleModuleSettingsClick(mouseX, mouseY, ModuleManager.get(selectedModuleId));
            return true;
        }
        if (selectedTab.equals("SETTINGS")) {
            handleGlobalSettingsClick(mouseX, mouseY);
            return true;
        }
        if (selectedTab.equals("PERFORMANCE")) {
            handlePerformanceClick(mouseX, mouseY);
            return true;
        }
        if (selectedTab.equals("STYLE")) {
            handleAppearanceClick(mouseX, mouseY);
            return true;
        }
        handleModuleClick(mouseX, mouseY);
        return true;
    }

    private void handleModuleClick(double mouseX, double mouseY) {
        int contentX = panelX + 20;
        int contentWidth = panelWidth - 40;
        int rowsY = panelY + 125 + 43;
        int rowStep = 60;
        int visibleRows = Math.max(1, (panelY + panelHeight - 57 - rowsY) / rowStep);
        int row = (int) ((mouseY - rowsY) / rowStep);
        int index = scrollOffset + row;
        List<Module> modules = filteredModules();
        if (row < 0 || row >= visibleRows || index < 0 || index >= modules.size()) return;
        Module module = modules.get(index);
        int y = rowsY + row * rowStep;
        int toggleX = contentX + contentWidth - 294;
        int bindX = toggleX + 74;
        int positionX = bindX + 88;
        int gearX = positionX + 88;
        if (inside(mouseX, mouseY, toggleX, y + 12, 68, 29)) {
            module.enabled = !module.enabled;
            if (module.id.equals("performance")) PerformanceProfile.setEnabled(Minecraft.getInstance(), module.enabled);
            ToastManager.show(module.name + (module.enabled ? " enabled" : " disabled"));
            save();
        } else if (inside(mouseX, mouseY, bindX, y + 12, 82, 29)) {
            bindingTarget = module.id;
        } else if (inside(mouseX, mouseY, positionX, y + 12, 82, 29)) {
            module.anchor = module.anchor.next();
            module.hasCustomPosition = false;
            save();
        } else if (inside(mouseX, mouseY, gearX, y + 12, 32, 29)) {
            selectedModuleId = module.id;
        }
    }

    private void handleGlobalSettingsClick(double mouseX, double mouseY) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int rowY = panelY + 125 + 43;
        int step = settingsStep();
        if (inside(mouseX, mouseY, panelButton(x, w, "theme"), rowY + 5, 112, 29)) {
            Config.data.theme = (Config.data.theme + 1) % Config.data.themeCount();
            Config.data.menuAccentColor = -1;
            Config.data.chroma = false;
        } else if (inside(mouseX, mouseY, x + w - 218, rowY + step + 3, 150, 34)) {
            activeSlider = "opacity";
            Config.data.hudOpacity = sliderValue(mouseX, x + w - 208, 140, 0, 100);
        } else if (inside(mouseX, mouseY, x + w - 218, rowY + step * 2 + 3, 150, 34)) {
            activeSlider = "menuOpacity";
            Config.data.menuOpacity = sliderValue(mouseX, x + w - 208, 140, 30, 100);
        } else if (inside(mouseX, mouseY, x + w - 218, rowY + step * 3 + 3, 150, 34)) {
            activeSlider = "hudScale";
            Config.data.hudScale = sliderValue(mouseX, x + w - 208, 140, 70, 150);
        } else if (inside(mouseX, mouseY, panelButton(x, w, "zoom"), rowY + step * 4 + 5, 150, 29)) {
            int zoomButtonX = panelButton(x, w, "zoom");
            Config.data.zoomFov = Config.clamp(Config.data.zoomFov + (mouseX > zoomButtonX + 75 ? 5 : -5), 30, 110);
        } else if (inside(mouseX, mouseY, panelButton(x, w, "crosshair"), rowY + step * 5 + 5, 150, 29)) {
            Config.data.crosshairSize = Config.clamp(Config.data.crosshairSize + (mouseX < x + w - 82 ? -1 : 1), 2, 12);
        } else if (inside(mouseX, mouseY, panelButton(x, w, "$menu"), rowY + step * 6 + 5, 150, 29)) {
            bindingTarget = "$menu";
        } else if (inside(mouseX, mouseY, panelButton(x, w, "$zoom"), rowY + step * 7 + 5, 150, 29)) {
            bindingTarget = "$zoom";
        } else if (inside(mouseX, mouseY, panelButton(x, w, "chroma"), rowY + step * 8 + 5, 150, 29)) {
            Config.data.chroma = !Boolean.TRUE.equals(Config.data.chroma);
        } else if (inside(mouseX, mouseY, panelButton(x, w, "compact"), rowY + step * 9 + 5, 150, 29)) {
            Config.data.compactHud = !Boolean.TRUE.equals(Config.data.compactHud);
        } else if (inside(mouseX, mouseY, panelButton(x, w, "bloom"), rowY + step * 10 + 5, 150, 29)) {
            Config.data.dynamicCrosshair = !Boolean.TRUE.equals(Config.data.dynamicCrosshair);
        } else if (inside(mouseX, mouseY, panelButton(x, w, "gridSnap"), rowY + step * 11 + 5, 150, 29)) {
            Config.data.gridSnap = !Boolean.TRUE.equals(Config.data.gridSnap);
        } else {
            return;
        }
        Config.save();
    }

    private void handlePerformanceClick(double mouseX, double mouseY) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int y = panelY + 125 + 48;
        Module profile = ModuleManager.get("performance");
        if (inside(mouseX, mouseY, x + w - 322, y + 15, 152, 28)) {
            Config.data.performanceMode = (Config.data.performanceMode + 1) % 2;
            PerformanceProfile.setMode(Minecraft.getInstance(), Config.data.performanceMode);
            Config.save();
            return;
        }
        if (!inside(mouseX, mouseY, x + w - 160, y + 15, 146, 28)) return;
        profile.enabled = !profile.enabled;
        PerformanceProfile.setEnabled(Minecraft.getInstance(), profile.enabled);
        ToastManager.show(profile.enabled ? "Performance profile applied" : "Graphics settings restored");
        save();
    }

    private void handleAppearanceClick(double mouseX, double mouseY) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int top = panelY + 125;
        int step = settingsStep();
        int rowY = top + 43;
        int resetWidth = 92;
        int gap = 5;
        int targetWidth = (w - resetWidth - gap * 4 - 10) / 4;
        int targetX = x + 6;
        for (int i = 0; i < 4; i++) {
            if (inside(mouseX, mouseY, targetX, rowY + 4, targetWidth, step - 12)) {
                styleColorTarget = i;
                return;
            }
            targetX += targetWidth + gap;
        }
        if (inside(mouseX, mouseY, x + w - resetWidth - 6, rowY + 4, resetWidth, step - 12)) {
            resetStyleColor();
            Config.save();
            return;
        }

        int paletteY = rowY + step;
        int swatchSize = Math.max(14, step - 12);
        int swatchGap = 5;
        int paletteWidth = COLOR_SWATCHES.length * swatchSize + (COLOR_SWATCHES.length - 1) * swatchGap;
        int swatchX = x + w - paletteWidth - 12;
        int swatchY = paletteY + (step - 4 - swatchSize) / 2;
        for (int i = 0; i < COLOR_SWATCHES.length; i++) {
            int sx = swatchX + i * (swatchSize + swatchGap);
            if (inside(mouseX, mouseY, sx - 1, swatchY - 1, swatchSize + 2, swatchSize + 2)) {
                applyStyleColor(COLOR_SWATCHES[i] & 0x00FFFFFF);
                Config.save();
                return;
            }
        }

        int rgbY = rowY + step * 2;
        for (int channel = 0; channel < 3; channel++) {
            if (inside(mouseX, mouseY, x + w - 218, rgbY + channel * step + 3, 150, step - 4)) {
                activeSlider = "menuRgb-" + channel;
                updateStyleColor(channel, mouseX);
                Config.save();
                return;
            }
        }

        int widthY = rowY + step * 5;
        if (inside(mouseX, mouseY, x + w - 218, widthY + 3, 150, step - 4)) {
            activeSlider = "menuWidth";
            Config.data.menuWidth = sliderValue(mouseX, x + w - 208, 140, 700, 1200);
            Config.save();
            return;
        }
        int heightY = rowY + step * 6;
        if (inside(mouseX, mouseY, x + w - 218, heightY + 3, 150, step - 4)) {
            activeSlider = "menuHeight";
            Config.data.menuHeight = sliderValue(mouseX, x + w - 208, 140, 540, 800);
            Config.save();
            return;
        }
        int dimY = rowY + step * 7;
        if (inside(mouseX, mouseY, x + w - 218, dimY + 3, 150, step - 4)) {
            activeSlider = "menuOverlayOpacity";
            Config.data.menuOverlayOpacity = sliderValue(mouseX, x + w - 208, 140, 0, 80);
            Config.save();
        }
    }

    private int styleColor() {
        return switch (styleColorTarget) {
            case 0 -> Config.data.menuAccentColor < 0 ? Config.data.accentColor() : Config.data.menuAccentColor;
            case 1 -> Config.data.menuPanelColor < 0 ? DEFAULT_PANEL_RGB : Config.data.menuPanelColor;
            case 2 -> Config.data.menuBorderColor < 0 ? DEFAULT_BORDER : Config.data.menuBorderColor;
            default -> Config.data.menuTextColor < 0 ? 0xFFF1F2F5 : Config.data.menuTextColor;
        } & 0x00FFFFFF;
    }

    private void updateStyleColor(int channel, double mouseX) {
        int value = sliderValue(mouseX, panelX + panelWidth - 228, 140, 0, 255);
        int color = styleColor();
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        if (channel == 0) red = value;
        else if (channel == 1) green = value;
        else blue = value;
        int updated = (red << 16) | (green << 8) | blue;
        applyStyleColor(updated);
    }

    private void applyStyleColor(int color) {
        int updated = color & 0x00FFFFFF;
        switch (styleColorTarget) {
            case 0 -> {
                Config.data.menuAccentColor = updated;
                Config.data.chroma = false;
            }
            case 1 -> Config.data.menuPanelColor = updated;
            case 2 -> Config.data.menuBorderColor = updated;
            default -> Config.data.menuTextColor = updated;
        }
    }

    private void resetStyleColor() {
        switch (styleColorTarget) {
            case 0 -> Config.data.menuAccentColor = -1;
            case 1 -> Config.data.menuPanelColor = -1;
            case 2 -> Config.data.menuBorderColor = -1;
            default -> Config.data.menuTextColor = -1;
        }
    }

    private void handleModuleSettingsClick(double mouseX, double mouseY, Module module) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int top = panelY + 125;
        if (inside(mouseX, mouseY, x, top, 76, 28)) {
            selectedModuleId = null;
            return;
        }
        int sliderX = x + Math.min(300, Math.max(220, w / 3));
        int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
        int rowY = top + 56;
        int base = module.customColor < 0 ? Config.data.accentColor() : module.customColor;
        int[] values = {(base >> 16) & 255, (base >> 8) & 255, base & 255};
        for (int channel = 0; channel < 3; channel++) {
            if (inside(mouseX, mouseY, sliderX - 4, rowY + channel * 48 + 4, sliderWidth + 48, 32)) {
                activeSlider = "rgb-" + channel;
                updateModuleRgb(module, channel, mouseX);
                save();
                return;
            }
        }
        int scaleY = rowY + 3 * 48;
        if (inside(mouseX, mouseY, sliderX - 4, scaleY + 4, sliderWidth + 48, 32)) {
            activeSlider = "moduleScale";
            module.scalePercent = sliderValue(mouseX, sliderX, sliderWidth, 70, 150);
            save();
            return;
        }
        if (inside(mouseX, mouseY, x + w - 138, scaleY + 5, 126, 27)) {
            module.customColor = -1;
            save();
            return;
        }
        rowY += 50;

        // Custom size buttons
        if (inside(mouseX, mouseY, x + w - 208, rowY + 6, 96, 27)) {
            module.customWidth = module.customWidth > 0 ? module.customWidth + 10 : module.width(Minecraft.getInstance()) + 10;
            save();
            return;
        }
        if (inside(mouseX, mouseY, x + w - 104, rowY + 6, 92, 27)) {
            module.customHeight = module.customHeight > 0 ? module.customHeight + 10 : module.height(Minecraft.getInstance()) + 10;
            save();
            return;
        }
        rowY += 50;

        // Width slider
        int currentWidth = module.customWidth > 0 ? module.customWidth : module.width(Minecraft.getInstance());
        if (inside(mouseX, mouseY, sliderX - 4, rowY + 4, sliderWidth + 48, 32)) {
            activeSlider = "moduleWidth";
            module.customWidth = sliderValue(mouseX, sliderX, sliderWidth, 10, 400);
            save();
            return;
        }
        rowY += 48;

        // Height slider
        int currentHeight = module.customHeight > 0 ? module.customHeight : module.height(Minecraft.getInstance());
        if (inside(mouseX, mouseY, sliderX - 4, rowY + 4, sliderWidth + 48, 32)) {
            activeSlider = "moduleHeight";
            module.customHeight = sliderValue(mouseX, sliderX, sliderWidth, 10, 400);
            save();
            return;
        }
        rowY += 50;

        // Padding slider
        if (inside(mouseX, mouseY, sliderX - 4, rowY + 4, sliderWidth + 48, 32)) {
            activeSlider = "modulePadding";
            module.padding = sliderValue(mouseX, sliderX, sliderWidth, 0, 20);
            save();
            return;
        }
        rowY += 50;

        // Border radius slider
        if (inside(mouseX, mouseY, sliderX - 4, rowY + 4, sliderWidth + 48, 32)) {
            activeSlider = "moduleBorderRadius";
            module.borderRadius = sliderValue(mouseX, sliderX, sliderWidth, 0, 16);
            save();
            return;
        }
        rowY += 50;

        // Opacity slider
        if (inside(mouseX, mouseY, sliderX - 4, rowY + 4, sliderWidth + 48, 32)) {
            activeSlider = "moduleOpacity";
            module.opacity = sliderValue(mouseX, sliderX, sliderWidth, 0, 100);
            save();
            return;
        }
        rowY += 50;

        // Border toggle
        if (inside(mouseX, mouseY, x + w - 174, rowY + 6, 162, 27)) {
            module.showBorder = !module.showBorder;
            save();
            return;
        }
        rowY += 50;

        int positionY = rowY;
        if (inside(mouseX, mouseY, x + w - 208, positionY + 6, 96, 27)) {
            module.anchor = module.anchor.next();
            module.hasCustomPosition = false;
            save();
            return;
        }
        if (inside(mouseX, mouseY, x + w - 104, positionY + 6, 92, 27)) {
            module.hasCustomPosition = false;
            module.customWidth = -1;
            module.customHeight = -1;
            save();
            return;
        }
        if (module.id.equals("health") || module.id.equals("hunger")) {
            if (inside(mouseX, mouseY, x + w - 174, positionY + 56, 162, 27)) {
                if (module.id.equals("health")) {
                    Config.data.healthFormat = (Config.data.healthFormat + 1) % 3;
                } else {
                    Config.data.hungerFormat = (Config.data.hungerFormat + 1) % 3;
                }
                save();
            } else if (inside(mouseX, mouseY, x + w - 174, positionY + 104, 162, 27)) {
                if (module.id.equals("health")) {
                    Config.data.vanillaHealthBar = !Boolean.TRUE.equals(Config.data.vanillaHealthBar);
                } else {
                    Config.data.vanillaFoodBar = !Boolean.TRUE.equals(Config.data.vanillaFoodBar);
                }
                save();
            }
            return;
        }
        if (module.id.equals("crosshair")) {
            int styleY = positionY + 50;
            if (inside(mouseX, mouseY, x + w - 174, styleY + 6, 162, 27)) {
                Config.data.crosshairShape = (Config.data.crosshairShape + 1) % 3;
            } else if (inside(mouseX, mouseY, x + w - 174, styleY + 54, 162, 27)) {
                Config.data.crosshairGap = (Config.data.crosshairGap + 1) % 13;
            } else {
                return;
            }
            Config.save();
        }
    }

    private static int sliderValue(double mouseX, int sliderX, int sliderWidth, int min, int max) {
        return Config.clamp(min + (int) Math.round((mouseX - sliderX) * (max - min) / sliderWidth), min, max);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selectedTab.equals("SETTINGS") || selectedTab.equals("PERFORMANCE") || selectedTab.equals("STYLE")
                || selectedModuleId != null) return true;
        int visibleRows = Math.max(1, (panelY + panelHeight - 57 - (panelY + 125 + 43)) / 60);
        int max = Math.max(0, filteredModules().size() - visibleRows);
        scrollOffset = Config.clamp(scrollOffset + (scrollY < 0 ? 1 : -1), 0, max);
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (activeSlider == null) return super.mouseDragged(event, deltaX, deltaY);
        int x = panelX + 20;
        int w = panelWidth - 40;
        if (activeSlider.equals("opacity")) {
            Config.data.hudOpacity = sliderValue(event.x(), x + w - 208, 140, 0, 100);
            Config.save();
            return true;
        }
        if (activeSlider.equals("hudScale")) {
            Config.data.hudScale = sliderValue(event.x(), x + w - 208, 140, 70, 150);
            Config.save();
            return true;
        }
        if (activeSlider.equals("menuOpacity")) {
            Config.data.menuOpacity = sliderValue(event.x(), x + w - 208, 140, 30, 100);
            Config.save();
            return true;
        }
        if (activeSlider.equals("menuWidth")) {
            Config.data.menuWidth = sliderValue(event.x(), x + w - 208, 140, 700, 1200);
            Config.save();
            return true;
        }
        if (activeSlider.equals("menuHeight")) {
            Config.data.menuHeight = sliderValue(event.x(), x + w - 208, 140, 540, 800);
            Config.save();
            return true;
        }
        if (activeSlider.startsWith("menuRgb-")) {
            updateStyleColor(Integer.parseInt(activeSlider.substring(8)), event.x());
            Config.save();
            return true;
        }
        if (activeSlider.equals("menuOverlayOpacity")) {
            Config.data.menuOverlayOpacity = sliderValue(event.x(), x + w - 208, 140, 0, 80);
            Config.save();
            return true;
        }
        if (selectedModuleId == null) return true;
        Module module = ModuleManager.get(selectedModuleId);
        if (activeSlider.startsWith("rgb-")) {
            int channel = Integer.parseInt(activeSlider.substring(4));
            updateModuleRgb(module, channel, event.x());
            save();
            return true;
        }
        if (activeSlider.equals("moduleScale")) {
            int sliderX = x + Math.min(300, Math.max(220, w / 3));
            int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
            module.scalePercent = sliderValue(event.x(), sliderX, sliderWidth, 70, 150);
            save();
        }
        if (activeSlider.equals("moduleWidth")) {
            int sliderX = x + Math.min(300, Math.max(220, w / 3));
            int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
            module.customWidth = sliderValue(event.x(), sliderX, sliderWidth, 10, 400);
            save();
        }
        if (activeSlider.equals("moduleHeight")) {
            int sliderX = x + Math.min(300, Math.max(220, w / 3));
            int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
            module.customHeight = sliderValue(event.x(), sliderX, sliderWidth, 10, 400);
            save();
        }
        if (activeSlider.equals("modulePadding")) {
            int sliderX = x + Math.min(300, Math.max(220, w / 3));
            int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
            module.padding = sliderValue(event.x(), sliderX, sliderWidth, 0, 20);
            save();
        }
        if (activeSlider.equals("moduleBorderRadius")) {
            int sliderX = x + Math.min(300, Math.max(220, w / 3));
            int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
            module.borderRadius = sliderValue(event.x(), sliderX, sliderWidth, 0, 16);
            save();
        }
        if (activeSlider.equals("moduleOpacity")) {
            int sliderX = x + Math.min(300, Math.max(220, w / 3));
            int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
            module.opacity = sliderValue(event.x(), sliderX, sliderWidth, 0, 100);
            save();
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        activeSlider = null;
        return super.mouseReleased(event);
    }

    private void updateModuleRgb(Module module, int channel, double mouseX) {
        int x = panelX + 20;
        int w = panelWidth - 40;
        int sliderX = x + Math.min(300, Math.max(220, w / 3));
        int sliderWidth = Math.max(130, Math.min(320, x + w - sliderX - 190));
        int value = sliderValue(mouseX, sliderX, sliderWidth, 0, 255);
        int color = module.customColor < 0 ? Config.data.accentColor() : module.customColor;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        if (channel == 0) red = value;
        else if (channel == 1) green = value;
        else blue = value;
        module.customColor = 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (bindingTarget != null || !searchFocused) return super.charTyped(event);
        if (event.isAllowedChatCharacter() && searchText.length() < 32) {
            searchText += event.codepointAsString();
            scrollOffset = 0;
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (bindingTarget != null) {
            int keyCode = event.key() == InputConstants.KEY_ESCAPE ? -1 : event.key();
            KeybindManager.bind(bindingTarget, keyCode);
            bindingTarget = null;
            return true;
        }
        if (searchFocused && event.key() == 259) {
            if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
            scrollOffset = 0;
            return true;
        }
        if (searchFocused && (event.key() == 257 || event.key() == 256)) {
            searchFocused = false;
            if (event.key() == 257) return true;
        }
        if (event.key() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        save();
        Minecraft.getInstance().gui.setScreen(parent);
    }

    private float lerpHover(String key, boolean over) {
        float current = hoverAmounts.getOrDefault(key, 0.0f);
        float next = current + ((over ? 1.0f : 0.0f) - current) * 0.28f;
        hoverAmounts.put(key, next);
        return next;
    }

    private static String anchorLabel(Anchor anchor) {
        return switch (anchor) {
            case TOP_LEFT -> "TL";
            case TOP_RIGHT -> "TR";
            case MIDDLE_LEFT -> "ML";
            case MIDDLE_RIGHT -> "MR";
            case BOTTOM_LEFT -> "BL";
            case BOTTOM_RIGHT -> "BR";
        };
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private static int withAlpha(int rgb, int alpha) {
        return (Config.clamp(alpha, 0, 255) << 24) | (rgb & 0x00FFFFFF);
    }

    private static void syncPalette() {
        int panel = Config.data.menuPanelColor < 0 ? DEFAULT_PANEL_RGB : Config.data.menuPanelColor;
        PANEL_RGB = panel & 0x00FFFFFF;
        HEADER_RGB = mix(PANEL_RGB, 0xFF252932, 0.36f) & 0x00FFFFFF;
        CARD_RGB = mix(PANEL_RGB, 0xFF272D36, 0.40f) & 0x00FFFFFF;
        CARD_HOVER_RGB = mix(CARD_RGB, Config.data.accentColor(), 0.22f) & 0x00FFFFFF;
        BORDER = Config.data.menuBorderColor < 0
                ? DEFAULT_BORDER : 0xFF000000 | (Config.data.menuBorderColor & 0x00FFFFFF);
        WHITE = Config.data.menuTextColor < 0
                ? 0xFFF1F2F5 : 0xFF000000 | (Config.data.menuTextColor & 0x00FFFFFF);
        MUTED = Config.data.menuTextColor < 0 ? 0xFF969BA8 : mix(WHITE, 0xFF969BA8, 0.52f);
    }

    private static int surface(int rgb, int extraAlpha) {
        int baseAlpha = Config.clamp(Config.data.menuOpacity, 30, 100) * 255 / 100;
        return withAlpha(rgb, Math.min(255, baseAlpha + extraAlpha));
    }

    private static int cardColor() {
        return surface(CARD_RGB, 42);
    }

    private static int cardHoverColor() {
        return surface(CARD_HOVER_RGB, 54);
    }

    private static int mix(int color, int other, float amount) {
        float t = Math.max(0.0f, Math.min(1.0f, amount));
        int r = Math.round(((color >> 16) & 255) * (1.0f - t) + ((other >> 16) & 255) * t);
        int g = Math.round(((color >> 8) & 255) * (1.0f - t) + ((other >> 8) & 255) * t);
        int b = Math.round((color & 255) * (1.0f - t) + (other & 255) * t);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
