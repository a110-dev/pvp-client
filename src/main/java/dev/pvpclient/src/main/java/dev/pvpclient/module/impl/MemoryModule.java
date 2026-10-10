package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

/** JVM heap usage readout, useful for spotting memory pressure. */
public final class MemoryModule extends TextModule {
    public MemoryModule() {
        super("memory", "Memory Usage", Anchor.TOP_RIGHT, false);
    }

    @Override
    protected String line(Minecraft mc) {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        return "Memory: " + toMb(used) + " / " + toMb(max) + " MB";
    }

    private static long toMb(long bytes) {
        return Math.round(bytes / (1024.0 * 1024.0));
    }
}
