package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.item.ItemStack;

/** Displays current hotbar slot and item durability. */
public final class HotbarModule extends Module {
    public HotbarModule() {
        super("hotbar", "Hotbar Info", Anchor.BOTTOM_RIGHT, false);
    }

    @Override
    public int width(Minecraft mc) {
        return 85;
    }

    @Override
    public int height(Minecraft mc) {
        return 22;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        ItemStack mainHand = mc.player.getMainHandItem();
        int slot = mc.player.getInventory().selected;
        int maxDurability = 0;
        int currentDurability = 0;

        if (mainHand != null && !mainHand.isEmpty()) {
            maxDurability = mainHand.getMaxDamage();
            currentDurability = maxDurability - mainHand.getDamage();
        }

        g.fill(x, y, x + width(mc), y + height(mc), backgroundColor());
        text(g, mc, "Slot: " + (slot + 1), x + padding, y + padding, WHITE);
        if (maxDurability > 0) {
            int percent = (currentDurability * 100) / maxDurability;
            int color = percent > 50 ? 0xFF66DB91 : percent > 25 ? 0xFFFFB86B : 0xFFFF6868;
            text(g, mc, percent + "%", x + padding, y + padding + 10, color);
        }
    }
}
