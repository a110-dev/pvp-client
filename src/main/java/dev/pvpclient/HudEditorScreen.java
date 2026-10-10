package dev.pvpclient;

import dev.pvpclient.module.Module;
import dev.pvpclient.module.ModuleManager;
import dev.pvpclient.module.ModuleManager.HudPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Free-position HUD editor with a draggable widget tray, resize handles, and optional grid snapping. */
public final class HudEditorScreen extends Screen {
    private static final int TRAY_WIDTH = 222;
    private static final int TRAY_TOP = 52;
    private static final int TRAY_ROW_HEIGHT = 23;
    private static final int GRID_SIZE = 4;
    private static final int HANDLE_SIZE = 8;
    private final Screen parent;
    private Module dragged;
    private boolean draggedFromTray;
    private boolean movedWhileDragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private int trayScroll;
    private boolean resizing;
    private int resizeHandle; // 0=SE, 1=SW, 2=NE, 3=NW
    private Module resizingModule;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Edit HUD"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x65080B11);
        Minecraft mc = Minecraft.getInstance();
        float scale = hudScale();
        int sw = Math.round(g.guiWidth() / scale);
        int sh = Math.round(g.guiHeight() / scale);
        ModuleManager.render(g, mc);

        Map<Module, HudPosition> positions = ModuleManager.layoutPositions(mc, sw, sh);
        for (Map.Entry<Module, HudPosition> entry : positions.entrySet()) {
            Module module = entry.getKey();
            HudPosition pos = entry.getValue();
            if (module.id.equals("crosshair")) continue;
            int x = Math.round(pos.x() * scale);
            int y = Math.round(pos.y() * scale);
            int w = Math.max(4, Math.round(pos.width() * scale));
            int h = Math.max(4, Math.round(pos.height() * scale));
            int color = module == dragged || module == resizingModule ? 0xFFFFD166 : Config.data.accentColor();
            outline(g, x - 2, y - 2, w + 4, h + 4, color);
            String label = module.name + "  " + module.scalePercent + "%";
            if (module.customWidth > 0 || module.customHeight > 0) {
                label += "  [" + module.customWidth + "x" + module.customHeight + "]";
            }
            drawTag(g, mc, label, x + 1, y - 15, color);

            // Draw resize handles
            drawResizeHandle(g, x + w - HANDLE_SIZE, y + h - HANDLE_SIZE, color); // SE
            drawResizeHandle(g, x, y + h - HANDLE_SIZE, color); // SW
            drawResizeHandle(g, x + w - HANDLE_SIZE, y, color); // NE
            drawResizeHandle(g, x, y, color); // NW
        }

        if (draggedFromTray && movedWhileDragging && !positions.containsKey(dragged)) {
            drawDraggedPlaceholder(g, mc, scale, dragged);
        }
        drawToolbar(g, mc);
        drawWidgetTray(g, mc);
    }

    private void drawToolbar(GuiGraphicsExtractor g, Minecraft mc) {
        String grid = Boolean.TRUE.equals(Config.data.gridSnap) ? "GRID  ON" : "GRID  OFF";
        drawToolbarButton(g, mc, 14, 14, 104, 26, grid,
                Boolean.TRUE.equals(Config.data.gridSnap) ? Config.data.accentColor() : 0xC822252D);
        drawToolbarButton(g, mc, 124, 14, 132, 26, "RESET POSITIONS", 0xC822252D);
        String hint = dragged == null
                ? "DRAG A WIDGET TO PLACE IT  ·  SCROLL TO RESIZE  ·  ESC TO FINISH"
                : "MOVING " + dragged.name.toUpperCase() + "  ·  RELEASE TO PLACE";
        int hintWidth = mc.font.width(hint) + 24;
        int hintX = Math.max(270, (width - TRAY_WIDTH - hintWidth) / 2);
        g.fill(hintX, 14, hintX + hintWidth, 40, 0xC8101218);
        g.text(mc.font, hint, hintX + 12, 23, 0xFFF1F2F5, true);
    }

    private void drawWidgetTray(GuiGraphicsExtractor g, Minecraft mc) {
        int x = width - TRAY_WIDTH - 12;
        int y = TRAY_TOP;
        int bottom = height - 14;
        int trayHeight = Math.max(120, bottom - y);
        g.fill(x, y, x + TRAY_WIDTH, bottom, menuSurface(0x0011161F, 16));
        outline(g, x, y, TRAY_WIDTH, trayHeight, 0xFF343947);
        g.fill(x + 1, y + 1, x + TRAY_WIDTH - 1, y + 42, menuSurface(0x00191D26, 32));
        g.text(mc.font, "WIDGET LIBRARY", x + 12, y + 9, 0xFFF1F2F5, true);
        g.text(mc.font, "Click to toggle  ·  Drag to place", x + 12, y + 25, 0xFF9BA3B2, true);

        List<Module> widgets = widgetModules();
        int listY = y + 48;
        int visibleRows = Math.max(1, (bottom - listY - 8) / TRAY_ROW_HEIGHT);
        trayScroll = Config.clamp(trayScroll, 0, Math.max(0, widgets.size() - visibleRows));
        for (int row = 0; row < visibleRows; row++) {
            int index = trayScroll + row;
            if (index >= widgets.size()) break;
            Module module = widgets.get(index);
            int rowY = listY + row * TRAY_ROW_HEIGHT;
            boolean active = module.enabled;
            int rowColor = module == dragged ? 0xD8433A27 : menuSurface(0x001D222C, 28);
            g.fill(x + 7, rowY, x + TRAY_WIDTH - 7, rowY + TRAY_ROW_HEIGHT - 2, rowColor);
            g.fill(x + 7, rowY, x + 10, rowY + TRAY_ROW_HEIGHT - 2,
                    active ? Config.data.accentColor() : 0xFF454B58);
            g.text(mc.font, module.name, x + 17, rowY + 5, active ? 0xFFF1F2F5 : 0xFFB5BAC4, true);
            String state = active ? "ON" : "OFF";
            int stateColor = active ? 0xFF78D9A1 : 0xFF858C99;
            g.text(mc.font, state, x + TRAY_WIDTH - 36 - mc.font.width(state), rowY + 5, stateColor, true);
        }
        if (widgets.size() > visibleRows) {
            int trackX = x + TRAY_WIDTH - 5;
            int trackY = listY;
            int trackHeight = visibleRows * TRAY_ROW_HEIGHT - 6;
            int thumbHeight = Math.max(24, trackHeight * visibleRows / widgets.size());
            int maxScroll = widgets.size() - visibleRows;
            int thumbY = trackY + (trackHeight - thumbHeight) * trayScroll / maxScroll;
            g.fill(trackX, trackY, trackX + 2, trackY + trackHeight, 0xFF343947);
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, Config.data.accentColor());
        }
    }

    private void drawDraggedPlaceholder(GuiGraphicsExtractor g, Minecraft mc, float scale, Module module) {
        int x = Math.round(module.customX * scale);
        int y = Math.round(module.customY * scale);
        float moduleScale = Config.clamp(module.scalePercent, 70, 150) / 100.0f;
        int w = Math.max(88, Math.round(module.width(mc) * moduleScale * scale));
        int h = Math.max(22, Math.round(module.height(mc) * moduleScale * scale));
        g.fill(x, y, x + w, y + h, menuSurface(0x0011161F, 100));
        outline(g, x, y, w, h, 0xFFFFD166);
        g.text(mc.font, module.name, x + 7, y + 6, 0xFFF1F2F5, true);
    }

    private void drawTag(GuiGraphicsExtractor g, Minecraft mc, String label, int x, int y, int accent) {
        int right = x + mc.font.width(label) + 10;
        g.fill(x - 2, y - 1, right, y + mc.font.lineHeight + 2, 0xD9111318);
        g.fill(x - 2, y - 1, x, y + mc.font.lineHeight + 2, accent);
        g.text(mc.font, label, x + 2, y + 1, 0xFFFFFFFF, true);
    }

    private void drawToolbarButton(GuiGraphicsExtractor g, Minecraft mc, int x, int y, int w, int h,
                                   String label, int color) {
        g.fill(x, y, x + w, y + h, color);
        outline(g, x, y, w, h, 0xFF343947);
        g.centeredText(mc.font, label, x + w / 2, y + (h - mc.font.lineHeight) / 2, 0xFFF1F2F5);
    }

    private void drawResizeHandle(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x, y, x + HANDLE_SIZE, y + HANDLE_SIZE, color);
        g.fill(x + 2, y + 2, x + HANDLE_SIZE - 2, y + HANDLE_SIZE - 2, 0x00000000);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 1) return true;
        double mouseX = event.x();
        double mouseY = event.y();
        if (inside(mouseX, mouseY, 14, 14, 104, 26)) {
            Config.data.gridSnap = !Boolean.TRUE.equals(Config.data.gridSnap);
            Config.save();
            return true;
        }
        if (inside(mouseX, mouseY, 124, 14, 132, 26)) {
            for (Module module : ModuleManager.all()) {
                module.hasCustomPosition = false;
                module.customWidth = -1;
                module.customHeight = -1;
            }
            ModuleManager.save();
            return true;
        }

        int trayX = width - TRAY_WIDTH - 12;
        int trayBottom = height - 14;
        if (inside(mouseX, mouseY, trayX, TRAY_TOP, TRAY_WIDTH, trayBottom - TRAY_TOP)) {
            int listY = TRAY_TOP + 48;
            if (mouseY >= listY) {
                int visibleRows = Math.max(1, (trayBottom - listY - 8) / TRAY_ROW_HEIGHT);
                int row = (int) ((mouseY - listY) / TRAY_ROW_HEIGHT);
                int index = trayScroll + row;
                List<Module> widgets = widgetModules();
                if (row >= 0 && row < visibleRows && index >= 0 && index < widgets.size()) {
                    dragged = widgets.get(index);
                    draggedFromTray = true;
                    movedWhileDragging = false;
                    dragOffsetX = 8;
                    dragOffsetY = 8;
                }
            }
            return true;
        }

        float scale = hudScale();
        int sw = Math.round(width / scale);
        int sh = Math.round(height / scale);
        List<Map.Entry<Module, HudPosition>> entries = new ArrayList<>(
                ModuleManager.layoutPositions(Minecraft.getInstance(), sw, sh).entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            Map.Entry<Module, HudPosition> entry = entries.get(i);
            Module module = entry.getKey();
            HudPosition pos = entry.getValue();
            if (module.id.equals("crosshair")) continue;
            double left = pos.x() * scale;
            double top = pos.y() * scale;
            double right = left + Math.max(4, pos.width() * scale);
            double bottom = top + Math.max(4, pos.height() * scale);

            // Check resize handles first
            if (mouseX >= right - HANDLE_SIZE - 2 && mouseX <= right + 2 &&
                mouseY >= bottom - HANDLE_SIZE - 2 && mouseY <= bottom + 2) {
                resizing = true;
                resizingModule = module;
                resizeHandle = 0; // SE
                return true;
            }
            if (mouseX >= left - 2 && mouseX <= left + HANDLE_SIZE + 2 &&
                mouseY >= bottom - HANDLE_SIZE - 2 && mouseY <= bottom + 2) {
                resizing = true;
                resizingModule = module;
                resizeHandle = 1; // SW
                return true;
            }
            if (mouseX >= right - HANDLE_SIZE - 2 && mouseX <= right + 2 &&
                mouseY >= top - 2 && mouseY <= top + HANDLE_SIZE + 2) {
                resizing = true;
                resizingModule = module;
                resizeHandle = 2; // NE
                return true;
            }
            if (mouseX >= left - 2 && mouseX <= left + HANDLE_SIZE + 2 &&
                mouseY >= top - 2 && mouseY <= top + HANDLE_SIZE + 2) {
                resizing = true;
                resizingModule = module;
                resizeHandle = 3; // NW
                return true;
            }

            // Check for dragging
            if (mouseX >= left - 2 && mouseX <= right + 2 && mouseY >= top - 2 && mouseY <= bottom + 2) {
                dragged = module;
                draggedFromTray = false;
                movedWhileDragging = false;
                dragOffsetX = (int) Math.round(mouseX - left);
                dragOffsetY = (int) Math.round(mouseY - top);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (resizing && resizingModule != null) {
            handleResize(event);
            return true;
        }
        if (dragged == null) return true;
        movedWhileDragging = true;
        if (draggedFromTray && !dragged.enabled) setModuleEnabled(dragged, true);
        float scale = hudScale();
        int x = Math.round((float) (event.x() - dragOffsetX) / scale);
        int y = Math.round((float) (event.y() - dragOffsetY) / scale);
        if (Boolean.TRUE.equals(Config.data.gridSnap)) {
            x = Math.round(x / (float) GRID_SIZE) * GRID_SIZE;
            y = Math.round(y / (float) GRID_SIZE) * GRID_SIZE;
        }
        int sw = Math.round(width / scale);
        int sh = Math.round(height / scale);
        Minecraft mc = Minecraft.getInstance();
        float moduleScale = Config.clamp(dragged.scalePercent, 70, 150) / 100.0f;
        int moduleWidth = Math.round(dragged.effectiveWidth(mc) * moduleScale);
        int moduleHeight = Math.round(dragged.effectiveHeight(mc) * moduleScale);
        dragged.customX = Config.clamp(x, 0, Math.max(0, sw - moduleWidth));
        dragged.customY = Config.clamp(y, 0, Math.max(0, sh - moduleHeight));
        dragged.hasCustomPosition = true;
        ModuleManager.save();
        return true;
    }

    private void handleResize(MouseButtonEvent event) {
        float scale = hudScale();
        Minecraft mc = Minecraft.getInstance();
        float moduleScale = Config.clamp(resizingModule.scalePercent, 70, 150) / 100.0f;
        int baseWidth = resizingModule.width(mc);
        int baseHeight = resizingModule.height(mc);

        // Calculate new dimensions based on handle
        double mouseX = event.x();
        double mouseY = event.y();
        float unscaledX = (float) (mouseX / scale);
        float unscaledY = (float) (mouseY / scale);

        if (Boolean.TRUE.equals(Config.data.gridSnap)) {
            unscaledX = Math.round(unscaledX / (float) GRID_SIZE) * GRID_SIZE;
            unscaledY = Math.round(unscaledY / (float) GRID_SIZE) * GRID_SIZE;
        }

        int newWidth = resizingModule.customWidth > 0 ? resizingModule.customWidth : baseWidth;
        int newHeight = resizingModule.customHeight > 0 ? resizingModule.customHeight : baseHeight;

        switch (resizeHandle) {
            case 0: // SE - resize width and height
                newWidth = Math.max(baseWidth / 2, Math.round(unscaledX - resizingModule.customX));
                newHeight = Math.max(baseHeight / 2, Math.round(unscaledY - resizingModule.customY));
                break;
            case 1: // SW - resize width and height
                newWidth = Math.max(baseWidth / 2, Math.round((resizingModule.customX + newWidth) - unscaledX));
                newHeight = Math.max(baseHeight / 2, Math.round(unscaledY - resizingModule.customY));
                break;
            case 2: // NE - resize width and height
                newWidth = Math.max(baseWidth / 2, Math.round(unscaledX - resizingModule.customX));
                newHeight = Math.max(baseHeight / 2, Math.round((resizingModule.customY + newHeight) - unscaledY));
                break;
            case 3: // NW - resize width and height
                newWidth = Math.max(baseWidth / 2, Math.round((resizingModule.customX + newWidth) - unscaledX));
                newHeight = Math.max(baseHeight / 2, Math.round((resizingModule.customY + newHeight) - unscaledY));
                break;
        }

        resizingModule.customWidth = newWidth;
        resizingModule.customHeight = newHeight;
        ModuleManager.save();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 1) {
            if (draggedFromTray && !movedWhileDragging && dragged != null) {
                setModuleEnabled(dragged, !dragged.enabled);
            }
            ModuleManager.save();
            dragged = null;
            draggedFromTray = false;
            movedWhileDragging = false;
            resizing = false;
            resizingModule = null;
            resizeHandle = -1;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dragged != null) {
            dragged.scalePercent = Config.clamp(dragged.scalePercent + (scrollY > 0 ? 5 : -5), 70, 150);
            ModuleManager.save();
            return true;
        }
        int trayX = width - TRAY_WIDTH - 12;
        int trayBottom = height - 14;
        int listY = TRAY_TOP + 48;
        if (inside(mouseX, mouseY, trayX, listY, TRAY_WIDTH, trayBottom - listY)) {
            int visibleRows = Math.max(1, (trayBottom - listY - 8) / TRAY_ROW_HEIGHT);
            int max = Math.max(0, widgetModules().size() - visibleRows);
            trayScroll = Config.clamp(trayScroll + (scrollY < 0 ? 1 : -1), 0, max);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        Config.save();
        Minecraft.getInstance().gui.setScreen(parent);
    }

    private static List<Module> widgetModules() {
        List<Module> widgets = new ArrayList<>();
        for (Module module : ModuleManager.all()) {
            if (!module.id.equals("crosshair")) widgets.add(module);
        }
        return widgets;
    }

    private static void setModuleEnabled(Module module, boolean enabled) {
        module.enabled = enabled;
        if (module.id.equals("performance")) {
            PerformanceProfile.setEnabled(Minecraft.getInstance(), enabled);
        }
    }

    private float hudScale() {
        return Config.clamp(Config.data.hudScale, 70, 150) / 100.0f;
    }

    private static int menuSurface(int rgb, int extraAlpha) {
        int base = Config.clamp(Config.data.menuOpacity, 30, 100) * 255 / 100;
        return ((Math.min(255, base + extraAlpha) << 24) | (rgb & 0x00FFFFFF));
    }

    private static boolean inside(double x, double y, int left, int top, int w, int h) {
        return x >= left && x < left + w && y >= top && y < top + h;
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        g.fill(x, y, x + width, y + 1, color);
        g.fill(x, y + height - 1, x + width, y + height, color);
        g.fill(x, y, x + 1, y + height, color);
        g.fill(x + width - 1, y, x + width, y + height, color);
    }
}
