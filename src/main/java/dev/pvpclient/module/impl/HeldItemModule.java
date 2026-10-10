package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** Main-hand item name and durability, if the item has it. */
public final class HeldItemModule extends TextModule {
    public HeldItemModule() {
        super("helditem", "Held Item", Anchor.BOTTOM_LEFT, false);
    }

    @Override
    public boolean visible(Minecraft mc) {
        return mc.player != null && !mc.player.getMainHandItem().isEmpty();
    }

    @Override
    protected String line(Minecraft mc) {
        if (mc.player == null) return "Holding: --";
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        String mainText = main.isEmpty() ? "--" : main.getHoverName().getString() + " x" + main.getCount();
        String offText = off.isEmpty() ? "--" : off.getHoverName().getString() + " x" + off.getCount();
        String durability = main.isDamageableItem()
                ? "  " + Math.max(0, main.getMaxDamage() - main.getDamageValue()) + " durability" : "";
        return "Main: " + mainText + durability + "  |  Off: " + offText;
    }
}
