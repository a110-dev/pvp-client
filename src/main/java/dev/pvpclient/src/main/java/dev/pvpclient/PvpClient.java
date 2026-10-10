package dev.pvpclient;

import dev.pvpclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;

public final class PvpClient implements ClientModInitializer {
    public static final String MOD_ID = "aleksa04_1";

    @Override
    public void onInitializeClient() {
        Config.load();
        ModuleManager.init();
        SolarCredits.registerTitleOverlay();

        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (world.isClientSide()) DamageFlash.trigger();
            return net.minecraft.world.InteractionResult.PASS;
        });

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        KeybindManager.init(category);

        Minecraft mc = Minecraft.getInstance();
        ZoomHandler zoom = new ZoomHandler(KeybindManager.zoomKey);
        if (ModuleManager.get("performance").enabled) {
            PerformanceProfile.setEnabled(mc, true);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeybindManager.menuKey.consumeClick()) {
                KeybindManager.toggleMenu(client);
            }
            KeybindManager.tickModules(client);
            zoom.tick(client);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            zoom.reset(client);
            PerformanceProfile.setEnabled(client, false);
        });

        ScreenEvents.AFTER_INIT.register((client, screen, screenWidth, screenHeight) -> {
            if (screen instanceof PauseScreen) {
                var widgets = Screens.getWidgets(screen);
                String optionsLabel = Component.translatable("menu.options").getString();
                String worldOptionsLabel = Component.translatable("options.worldOptions.button").getString();
                int optionsY = -1;
                for (AbstractWidget widget : widgets) {
                    String label = widget.getMessage().getString();
                    if (label.equals(optionsLabel) || label.equals(worldOptionsLabel)) {
                        optionsY = optionsY < 0 ? widget.getY() : Math.min(optionsY, widget.getY());
                    }
                }

                if (optionsY >= 0) {
                    // Reserve one standard pause-menu row for the client button. Only
                    // move the Options row and the immediately following action row.
                    for (AbstractWidget widget : widgets) {
                        int centerX = widget.getX() + widget.getWidth() / 2;
                        boolean inPauseMenuColumn = Math.abs(centerX - screenWidth / 2) <= 170;
                        if (inPauseMenuColumn && widget.getY() >= optionsY
                                && widget.getY() <= optionsY + 48) {
                            widget.setY(widget.getY() + 28);
                        }
                    }

                    Button clientMenu = Button.builder(Component.literal("PVP CLIENT"),
                                    button -> client.gui.setScreen(new ModuleScreen(screen)))
                            .bounds(screenWidth / 2 - 81, optionsY, 162, 20)
                            .build();
                    widgets.add(clientMenu);
                }
            }

            if (!(screen instanceof PauseScreen || screen instanceof DeathScreen)
                    || client.getCurrentServer() == null || client.isLocalServer()) return;
            Button reconnect = Button.builder(Component.literal("Quick Reconnect"),
                    button -> ReconnectManager.reconnect(client, screen))
                    .bounds(screenWidth / 2 - 100, screenHeight - 48, 200, 20)
                    .build();
            Screens.getWidgets(screen).add(reconnect);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hud"), (graphics, deltaTracker) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.gui.hud.isHidden()) return;
            SolarCredits.renderHud(graphics);
            ClickTracker.update(client);
            ModuleManager.render(graphics, client);
            DamageFlash.render(graphics, client);
            ToastManager.render(graphics, client);
        });
    }
}
