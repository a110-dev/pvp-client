package dev.pvpclient;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Adds a "Configure" button for aleksa04_1 in Mod Menu. Mod Menu is optional: this class is only
 * loaded through the "modmenu" entrypoint, so the client runs fine without it installed.
 */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModuleScreen::new;
    }
}
