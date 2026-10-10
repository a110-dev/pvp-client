package dev.pvpclient.module;

import dev.pvpclient.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A single HUD element. Position is decided by {@link ModuleManager} from the anchor. */
public abstract class Module {
    public static final int BG = 0x90000000;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int PAD = 3;

    public final String id;
    public final String name;
    public final Anchor defaultAnchor;
    public final boolean defaultEnabled;

    public boolean enabled;
    public Anchor anchor;
    public int keyCode = -1;
    public int customColor = -1;
    public int scalePercent = 100;
    public int customX;
    public int customY;
    public boolean hasCustomPosition;
    public KeyMapping keyBinding;
    public int customWidth = -1;
    public int customHeight = -1;
    public int padding = PAD;
    public int borderRadius = 0;
    public int backgroundColor = -1;
    public boolean showBorder = false;
    public int borderColor = -1;
    public int opacity = -1;

    protected Module(String id, String name, Anchor defaultAnchor, boolean defaultEnabled) {
        this.id = id;
        this.name = name;
        this.defaultAnchor = defaultAnchor;
        this.defaultEnabled = defaultEnabled;
        this.enabled = defaultEnabled;
        this.anchor = defaultAnchor;
    }

    public abstract int width(Minecraft mc);

    public abstract int height(Minecraft mc);

    public int effectiveWidth(Minecraft mc) {
        return customWidth > 0 ? customWidth : width(mc);
    }

    public int effectiveHeight(Minecraft mc) {
        return customHeight > 0 ? customHeight : height(mc);
    }

    public abstract void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y);

    /** Return false to take no space this frame (e.g. no potion effects active). */
    public boolean visible(Minecraft mc) {
        return true;
    }

    public String category() {
        return switch (id) {
            case "crosshair", "fpsgraph" -> "RENDER";
            case "sprint", "sneak" -> "MOVEMENT";
            case "performance", "memory", "clock", "speed", "dimension", "day", "weather", "biome", "server", "timer" -> "UTILITY";
            case "health", "hunger" -> "HUD";
            case "cps", "keystrokes", "armor", "potions", "helditem", "durability", "ping", "hitflash", "targetdistance", "hotbar", "xp" -> "COMBAT";
            default -> "HUD";
        };
    }

    public String description() {
        return switch (id) {
            case "fps" -> "Live frame rate readout";
            case "fpsgraph" -> "Scrolling frame-time snapshot";
            case "cps" -> "Left and right clicks per second";
            case "ping" -> "Server latency in milliseconds";
            case "coords" -> "Player position in the world";
            case "keystrokes" -> "Movement, mouse and jump display";
            case "armor" -> "Armor durability at a glance";
            case "potions" -> "Active effects and remaining time";
            case "health" -> "Movable health display; replaces vanilla hearts when enabled";
            case "hunger" -> "Movable food display; replaces the vanilla food row when enabled";
            case "biome" -> "Name of the biome you are in";
            case "helditem" -> "Main- and off-hand item counts and durability";
            case "memory" -> "Java memory use and available heap";
            case "durability" -> "Warns when worn armor is nearly broken";
            case "hitflash" -> "Brief custom-color flash on entity attack input";
            case "targetdistance" -> "Read-only distance to the entity under the crosshair";
            case "direction" -> "Cardinal direction and yaw";
            case "clock" -> "Local clock, down to the second";
            case "speed" -> "Current horizontal movement speed";
            case "dimension" -> "Current world dimension";
            case "day" -> "Minecraft world day counter";
            case "weather" -> "Rain and clear-weather status";
            case "sprint" -> "Sprint while moving forward";
            case "sneak" -> "Shows when crouching";
            case "crosshair" -> "Minimal accent-colored center mark";
            case "hotbar" -> "Current hotbar slot and item durability";
            case "server" -> "Current server name and IP";
            case "timer" -> "Session play time tracker";
            case "xp" -> "Experience level and progress bar";
            default -> "Client-side HUD module";
        };
    }

    public static int backgroundColor() {
        int opacity = Config.clamp(Config.data.hudOpacity, 0, 100);
        int alpha = Math.round(opacity * 2.55f);
        return (alpha << 24) | 0x00101924;
    }

    public int accentColor() {
        return customColor < 0 ? Config.data.accentColor() : customColor | 0xFF000000;
    }

    protected static void text(GuiGraphicsExtractor g, Minecraft mc, String s, int x, int y, int color) {
        g.text(mc.font, s, x, y, color, true);
    }
}
