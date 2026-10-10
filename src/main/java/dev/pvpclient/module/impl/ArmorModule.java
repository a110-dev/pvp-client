package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Worn armor with remaining durability. */
public final class ArmorModule extends Module {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int ROW = 21;
    private static final int WIDTH = 16 + 4 + 30 + PAD;

    public ArmorModule() {
        super("armor", "Armor Status", Anchor.MIDDLE_RIGHT, true);
    }

    @Override
    public boolean visible(Minecraft mc) {
        if (mc.player == null) {
            return false;
        }
        for (EquipmentSlot slot : SLOTS) {
            if (!mc.player.getItemBySlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int width(Minecraft mc) {
        return WIDTH;
    }

    @Override
    public int height(Minecraft mc) {
        return ROW * SLOTS.length + 2;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        g.fill(x, y, x + width(mc), y + height(mc), backgroundColor());
        g.fill(x, y, x + 2, y + height(mc), accentColor());

        int rowY = y + 1;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                g.item(stack, x + 2, rowY);
                g.itemDecorations(mc.font, stack, x + 2, rowY);

                if (stack.isDamageableItem()) {
                    int max = stack.getMaxDamage();
                    int left = max - stack.getDamageValue();
                    float frac = max == 0 ? 1f : (float) left / max;
                    int color = frac > 0.5f ? 0xFF55FF55 : frac > 0.2f ? 0xFFFFFF55 : 0xFFFF5555;
                    text(g, mc, Integer.toString(left), x + 2 + 16 + 4, rowY + 5, color);
                    g.fill(x + 2, rowY + 17, x + 18, rowY + 19, 0xFF313844);
                    g.fill(x + 2, rowY + 17, x + 2 + Math.max(1, Math.round(16 * frac)), rowY + 19, color);
                }
            }
            rowY += ROW;
        }
    }
}
