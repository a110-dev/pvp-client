package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

import java.util.Locale;

/** Current biome name, formatted for a small HUD tile. */
public final class BiomeModule extends TextModule {
    public BiomeModule() {
        super("biome", "Biome", Anchor.TOP_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null && mc.level != null;
    }

    @Override
    protected String line(Minecraft mc) {
        if (mc.player == null || mc.level == null) return "Biome: --";
        String id = mc.level.getBiome(mc.player.blockPosition()).unwrapKey()
                .map(key -> key.identifier().getPath()).orElse("unknown");
        String name = id.replace('_', ' ');
        StringBuilder title = new StringBuilder(name.length());
        boolean capitalize = true;
        for (char c : name.toCharArray()) {
            title.append(capitalize ? Character.toUpperCase(c) : c);
            capitalize = c == ' ';
        }
        return "Biome: " + title;
    }
}
