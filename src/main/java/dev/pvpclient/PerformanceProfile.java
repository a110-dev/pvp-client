package dev.pvpclient;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;

/** Applies a reversible, client-only graphics preset for lower-end systems. */
public final class PerformanceProfile {
    private record Snapshot(int renderDistance, int simulationDistance, double entityDistance,
                            ParticleStatus particles, CloudStatus clouds, int weatherRadius,
                            boolean ambientOcclusion, boolean entityShadows) {
    }

    private static Snapshot snapshot;
    private static boolean active;

    public static void setEnabled(Minecraft mc, boolean enabled) {
        if (enabled && !active) {
            snapshot = new Snapshot(
                    mc.options.renderDistance().get(),
                    mc.options.simulationDistance().get(),
                    mc.options.entityDistanceScaling().get(),
                    mc.options.particles().get(),
                    mc.options.cloudStatus().get(),
                    mc.options.weatherRadius().get(),
                    mc.options.ambientOcclusion().get(),
                    mc.options.entityShadows().get());

            active = true;
            applyPreset(mc);
        } else if (!enabled && active && snapshot != null) {
            mc.options.renderDistance().set(snapshot.renderDistance());
            mc.options.simulationDistance().set(snapshot.simulationDistance());
            mc.options.entityDistanceScaling().set(snapshot.entityDistance());
            mc.options.particles().set(snapshot.particles());
            mc.options.cloudStatus().set(snapshot.clouds());
            mc.options.weatherRadius().set(snapshot.weatherRadius());
            mc.options.ambientOcclusion().set(snapshot.ambientOcclusion());
            mc.options.entityShadows().set(snapshot.entityShadows());
            mc.options.save();
            snapshot = null;
            active = false;
        }
    }

    public static void setMode(Minecraft mc, int mode) {
        Config.data.performanceMode = Math.floorMod(mode, 2);
        if (active && snapshot != null) applyPreset(mc);
        Config.save();
    }

    private static void applyPreset(Minecraft mc) {
        if (snapshot == null) return;
        boolean maxFps = Config.data.performanceMode == 1;
        mc.options.renderDistance().set(Math.min(snapshot.renderDistance(), maxFps ? 4 : 8));
        mc.options.simulationDistance().set(Math.min(snapshot.simulationDistance(), maxFps ? 4 : 6));
        mc.options.entityDistanceScaling().set(Math.min(snapshot.entityDistance(), maxFps ? 0.50 : 0.75));
        mc.options.particles().set(ParticleStatus.MINIMAL);
        mc.options.cloudStatus().set(CloudStatus.OFF);
        mc.options.weatherRadius().set(0);
        mc.options.ambientOcclusion().set(false);
        mc.options.entityShadows().set(false);
        mc.options.save();
    }

    public static boolean isActive() {
        return active;
    }

    private PerformanceProfile() {
    }
}
