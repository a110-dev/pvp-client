package dev.pvpclient.module.impl;

import dev.pvpclient.module.Anchor;
import dev.pvpclient.module.TextModule;
import net.minecraft.client.Minecraft;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ClockModule extends TextModule {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClockModule() {
        super("clock", "Clock", Anchor.TOP_RIGHT, false);
    }

    @Override
    protected String line(Minecraft mc) {
        return "Time: " + LocalTime.now().format(FORMAT);
    }
}
