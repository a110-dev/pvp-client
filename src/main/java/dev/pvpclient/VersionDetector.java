package dev.pvpclient;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

/** Read-only information about the Minecraft and Fabric runtime hosting this mod. */
public final class VersionDetector {
    public record RuntimeInfo(String minecraftVersion, String loaderName, String loaderVersion) {
        public String displayLine() {
            return "MINECRAFT " + minecraftVersion + "  /  " + loaderName + " " + loaderVersion;
        }
    }

    public static RuntimeInfo detect() {
        String minecraftVersion = SharedConstants.getCurrentVersion().name();
        FabricLoader loader = FabricLoader.getInstance();
        var quiltContainer = loader.getModContainer("quilt_loader");
        String loaderName = quiltContainer.isPresent() ? "QUILT" : "FABRIC";
        String loaderVersion = quiltContainer.or(() -> loader.getModContainer("fabricloader"))
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        return new RuntimeInfo(minecraftVersion, loaderName, loaderVersion);
    }

    private VersionDetector() {
    }
}
