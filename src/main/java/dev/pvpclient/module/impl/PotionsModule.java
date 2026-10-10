package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import dev.pvpclient.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Active potion effects with level and time left. */
public final class PotionsModule extends Module {
    public PotionsModule() {
        super("potions", "Potion Effects", Anchor.MIDDLE_LEFT, true);
    }

    private List<String> lines(Minecraft mc) {
        List<String> out = new ArrayList<>();
        if (mc.player == null) {
            return out;
        }
        for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            String name = effect.getEffect().value().getDisplayName().getString();
            String time;
            if (effect.isInfiniteDuration()) {
                time = "**:**";
            } else {
                int seconds = effect.getDuration() / 20;
                time = String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
            }
            out.add(name + " " + (effect.getAmplifier() + 1) + "  " + time);
        }
        return out;
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null && !mc.player.getActiveEffects().isEmpty();
    }

    @Override
    public int width(Minecraft mc) {
        int w = 0;
        for (String s : lines(mc)) {
            w = Math.max(w, mc.font.width(s));
        }
        return w + PAD * 2;
    }

    @Override
    public int height(Minecraft mc) {
        return lines(mc).size() * (mc.font.lineHeight + 2) + PAD * 2 - 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        g.fill(x, y, x + width(mc), y + height(mc), backgroundColor());
        g.fill(x, y, x + 2, y + height(mc), accentColor());
        int ly = y + PAD;
        for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            int seconds = effect.isInfiniteDuration() ? Integer.MAX_VALUE : effect.getDuration() / 20;
            int color = seconds <= 10 ? 0xFFFF6969 : seconds <= 30 ? 0xFFFFC66B : WHITE;
            String name = effect.getEffect().value().getDisplayName().getString();
            String time = effect.isInfiniteDuration() ? "**:**" : String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
            text(g, mc, name + " " + (effect.getAmplifier() + 1) + "  " + time, x + PAD, ly, color);
            ly += mc.font.lineHeight + 2;
        }
    }
}
