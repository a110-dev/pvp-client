package dev.pvpclient.module.impl;

import dev.pvpclient.Config;

import java.util.Locale;

/** Shared formatting for the movable health and hunger HUD widgets. */
final class VitalDisplay {
    private VitalDisplay() {
    }

    static String format(String label, float current, float maximum, int mode, String unit) {
        float ratio = maximum <= 0.0f ? 0.0f : Math.max(0.0f, Math.min(1.0f, current / maximum));
        int percent = Math.round(ratio * 100.0f);
        return switch (Math.floorMod(mode, 3)) {
            case 1 -> label + ": " + percent + "%";
            case 2 -> label + ": [" + bar(ratio) + "] " + percent + "%";
            default -> label + ": " + heartValue(ratio * 10.0f) + "/10 " + unit;
        };
    }

    private static String bar(float ratio) {
        int filled = Config.clamp(Math.round(ratio * 10.0f), 0, 10);
        return "#".repeat(filled) + "-".repeat(10 - filled);
    }

    private static String heartValue(float value) {
        float rounded = Math.round(value * 2.0f) / 2.0f;
        return rounded == Math.floor(rounded)
                ? String.format(Locale.ROOT, "%.0f", rounded)
                : String.format(Locale.ROOT, "%.1f", rounded);
    }
}
