package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Shows the most worn equipped armor piece when it is close to breaking. */
public final class DurabilityAlertModule extends TextModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] NAMES = {"Helmet", "Chestplate", "Leggings", "Boots"};
    private static final int ALERT_PERCENT = 20;

    public DurabilityAlertModule() {
        super("durability", "Armor Warning", Anchor.TOP_RIGHT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return weakestArmor(mc) != null;
    }

    @Override
    protected String line(Minecraft mc) {
        ArmorState state = weakestArmor(mc);
        return state == null ? "Armor OK" : "Armor low: " + NAMES[state.slotIndex] + " " + state.percent + "%";
    }

    private static ArmorState weakestArmor(Minecraft mc) {
        if (mc.player == null) return null;
        ArmorState weakest = null;
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack item = mc.player.getItemBySlot(SLOTS[i]);
            if (!item.isEmpty() && item.isDamageableItem()) {
                int max = item.getMaxDamage();
                int left = Math.max(0, max - item.getDamageValue());
                int percent = max <= 0 ? 100 : left * 100 / max;
                if (percent <= ALERT_PERCENT && (weakest == null || percent < weakest.percent)) {
                    weakest = new ArmorState(i, percent);
                }
            }
        }
        return weakest;
    }

    private record ArmorState(int slotIndex, int percent) {
    }
}
