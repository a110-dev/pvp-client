package dev.pvpclient;

import com.mojang.blaze3d.platform.NativeImage;
import dev.pvpclient.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Small in-game screenshot browser with preview, image copy, and confirmed delete. */
public final class ScreenshotManagerScreen extends Screen {
    private static final Identifier PREVIEW_TEXTURE = Identifier.fromNamespaceAndPath("aleksa04_1", "screenshot_preview");
    private static final int PANEL = 0xEE111318;
    private static final int BORDER = 0xFF2B2E38;
    private final Screen parent;
    private final List<Path> screenshots = new ArrayList<>();
    private Path selected;
    private boolean confirmDelete;
    private String status = "";
    private int listOffset;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int imageWidth;
    private int imageHeight;

    public ScreenshotManagerScreen(Screen parent) {
        super(Component.literal("Screenshot Gallery"));
        this.parent = parent;
        refresh();
    }

    private void refresh() {
        Path directory = Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots");
        try {
            Files.createDirectories(directory);
            screenshots.clear();
            try (var files = Files.list(directory)) {
                files.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".png"))
                        .sorted(Comparator.comparingLong(ScreenshotManagerScreen::modifiedTime).reversed())
                        .forEach(screenshots::add);
            }
            if (selected == null || !screenshots.contains(selected)) selected = screenshots.isEmpty() ? null : screenshots.get(0);
            loadPreview();
        } catch (IOException exception) {
            status = "Could not read screenshots folder";
        }
    }

    private static long modifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private void loadPreview() {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().release(PREVIEW_TEXTURE);
        imageWidth = 0;
        imageHeight = 0;
        if (selected == null) return;
        try (var input = Files.newInputStream(selected)) {
            NativeImage image = NativeImage.read(input);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
            DynamicTexture texture = new DynamicTexture(() -> selected.getFileName().toString(), image);
            mc.getTextureManager().register(PREVIEW_TEXTURE, texture);
        } catch (IOException exception) {
            status = "Could not preview this screenshot";
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.fill(0, 0, width, height, 0x80000000);
        panelWidth = Math.min(820, Math.max(620, width - 32));
        panelHeight = Math.min(560, Math.max(420, height - 32));
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        g.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL);
        outline(g, panelX, panelY, panelWidth, panelHeight, BORDER);
        g.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + 54, 0xFF181A20);
        g.text(font, Component.literal("SCREENSHOT GALLERY").withStyle(style -> style.withBold(true)), panelX + 20, panelY + 18, 0xFFF1F2F5);
        g.text(font, "Click a capture to preview it. Delete requires a second confirmation.", panelX + 20, panelY + 37, 0xFF969BA8);

        int listX = panelX + 18;
        int listY = panelY + 72;
        int listWidth = Math.min(330, panelWidth / 2 - 24);
        int visibleRows = Math.max(1, (panelHeight - 150) / 40);
        for (int row = 0; row < visibleRows; row++) {
            int index = listOffset + row;
            if (index >= screenshots.size()) break;
            Path path = screenshots.get(index);
            int y = listY + row * 40;
            boolean chosen = path.equals(selected);
            int bg = chosen ? 0xFF252A34 : (mouseX >= listX && mouseX < listX + listWidth && mouseY >= y && mouseY < y + 35 ? 0xFF20242D : 0xFF191C23);
            g.fill(listX, y, listX + listWidth, y + 35, bg);
            outline(g, listX, y, listWidth, 35, chosen ? Config.data.accentColor() : BORDER);
            String name = path.getFileName().toString();
            int maxWidth = listWidth - 20;
            while (font.width(name) > maxWidth && name.length() > 8) name = name.substring(0, name.length() - 4) + "…png";
            g.text(font, name, listX + 10, y + 12, 0xFFF1F2F5);
        }
        if (screenshots.isEmpty()) g.text(font, "No screenshots found yet.", listX + 8, listY + 12, 0xFF969BA8);

        int previewX = listX + listWidth + 18;
        int previewY = listY;
        int previewWidth = panelX + panelWidth - 18 - previewX;
        int previewHeight = panelHeight - 150;
        g.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, 0xFF0B0D11);
        outline(g, previewX, previewY, previewWidth, previewHeight, BORDER);
        if (selected != null && imageWidth > 0 && imageHeight > 0) {
            float fit = Math.min((previewWidth - 16) / (float) imageWidth, (previewHeight - 16) / (float) imageHeight);
            int drawWidth = Math.max(1, Math.round(imageWidth * fit));
            int drawHeight = Math.max(1, Math.round(imageHeight * fit));
            int drawX = previewX + (previewWidth - drawWidth) / 2;
            int drawY = previewY + (previewHeight - drawHeight) / 2;
            g.blit(PREVIEW_TEXTURE, drawX, drawY, drawWidth, drawHeight, 0, 0, 1, 1);
        }
        if (!status.isEmpty()) g.text(font, status, previewX + 10, previewY + previewHeight - 22, 0xFFFFC66B);

        int buttonsY = panelY + panelHeight - 54;
        drawButton(g, mouseX, mouseY, panelX + 18, buttonsY, 98, 30, "COPY IMAGE", selected != null);
        drawButton(g, mouseX, mouseY, panelX + 124, buttonsY, 98, 30,
                confirmDelete ? "CONFIRM" : "DELETE", selected != null);
        drawButton(g, mouseX, mouseY, panelX + panelWidth - 104, buttonsY, 86, 30, "CLOSE", true);
        g.text(font, "{" + screenshots.size() + "} captures", panelX + 238, buttonsY + 10, 0xFF969BA8);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 1) return true;
        int listX = panelX + 18;
        int listY = panelY + 72;
        int listWidth = Math.min(330, panelWidth / 2 - 24);
        int visibleRows = Math.max(1, (panelHeight - 150) / 40);
        for (int row = 0; row < visibleRows; row++) {
            int index = listOffset + row;
            int y = listY + row * 40;
            if (index < screenshots.size() && event.x() >= listX && event.x() < listX + listWidth && event.y() >= y && event.y() < y + 35) {
                selected = screenshots.get(index);
                confirmDelete = false;
                status = "";
                loadPreview();
                return true;
            }
        }
        int buttonsY = panelY + panelHeight - 54;
        if (inside(event.x(), event.y(), panelX + 18, buttonsY, 98, 30)) {
            copyImage();
        } else if (inside(event.x(), event.y(), panelX + 124, buttonsY, 98, 30)) {
            deleteSelected();
        } else if (inside(event.x(), event.y(), panelX + panelWidth - 104, buttonsY, 86, 30)) {
            onClose();
        }
        return true;
    }

    private void copyImage() {
        if (selected == null) return;
        try {
            BufferedImage image = ImageIO.read(selected.toFile());
            if (image == null) throw new IOException("Unsupported screenshot format");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageTransferable(image), null);
            status = "Image copied to clipboard";
            confirmDelete = false;
        } catch (Exception exception) {
            status = "Could not copy image to clipboard";
        }
    }

    private void deleteSelected() {
        if (selected == null) return;
        if (!confirmDelete) {
            confirmDelete = true;
            status = "Click DELETE again to remove this file";
            return;
        }
        try {
            Files.deleteIfExists(selected);
            selected = null;
            confirmDelete = false;
            status = "Screenshot deleted";
            refresh();
        } catch (IOException exception) {
            status = "Could not delete this screenshot";
            confirmDelete = false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleRows = Math.max(1, (panelHeight - 150) / 40);
        listOffset = Config.clamp(listOffset + (scrollY < 0 ? 1 : -1), 0, Math.max(0, screenshots.size() - visibleRows));
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
        Minecraft.getInstance().getTextureManager().release(PREVIEW_TEXTURE);
        ModuleManager.save();
        Config.save();
        Minecraft.getInstance().gui.setScreen(parent);
    }

    private void drawButton(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h, String label, boolean enabled) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int color = !enabled ? 0xFF252832 : hover ? 0xFF323844 : 0xFF272B34;
        g.fill(x, y, x + w, y + h, color);
        outline(g, x, y, w, h, BORDER);
        g.centeredText(font, label, x + w / 2, y + (h - font.lineHeight) / 2, enabled ? 0xFFF1F2F5 : 0xFF707580);
    }

    private static boolean inside(double x, double y, int left, int top, int w, int h) {
        return x >= left && x < left + w && y >= top && y < top + h;
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static final class ImageTransferable implements Transferable {
        private final Image image;

        private ImageTransferable(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
            return image;
        }
    }
}
