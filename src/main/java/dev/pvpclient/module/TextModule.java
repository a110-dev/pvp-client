package dev.pvpclient.module;

import dev.pvpclient.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A one-line text readout on a translucent background. */
public abstract class TextModule extends Module {
    protected TextModule(String id, String name, Anchor defaultAnchor, boolean defaultEnabled) {
        super(id, name, defaultAnchor, defaultEnabled);
    }

    protected abstract String line(Minecraft mc);

    @Override
    public int width(Minecraft mc) {
        int padding = Boolean.TRUE.equals(Config.data.compactHud) ? PAD : PAD + 4;
        int lineWidth = mc.font.width(line(mc));
        if (!Boolean.TRUE.equals(Config.data.compactHud)) lineWidth = Math.max(lineWidth, mc.font.width(name));
        return lineWidth + padding * 2;
    }

    @Override
    public int height(Minecraft mc) {
        int padding = Boolean.TRUE.equals(Config.data.compactHud) ? PAD : PAD + 4;
        return Boolean.TRUE.equals(Config.data.compactHud)
                ? mc.font.lineHeight + padding * 2
                : mc.font.lineHeight * 2 + padding * 2 + 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        g.fill(x, y, x + width(mc), y + height(mc), backgroundColor());
        g.fill(x, y, x + 2, y + height(mc), accentColor());
        boolean compact = Boolean.TRUE.equals(Config.data.compactHud);
        int padding = compact ? PAD : PAD + 4;
        if (!compact) text(g, mc, name, x + padding, y + padding, accentColor());
        text(g, mc, line(mc), x + padding, y + padding + (compact ? 0 : mc.font.lineHeight + 2),
                customColor < 0 ? WHITE : accentColor());
    }
}
