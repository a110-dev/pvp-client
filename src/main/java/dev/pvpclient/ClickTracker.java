package dev.pvpclient;

import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;

/**
 * Counts attack/use presses per second. Sampled once per rendered frame by
 * polling the key mappings, so it needs no mixins and tracks frame rate, not tick rate.
 */
public final class ClickTracker {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean leftDown;
    private static boolean rightDown;
    private static long lastLeftClickAt;

    public static void update(Minecraft mc) {
        long now = System.currentTimeMillis();

        boolean l = mc.options.keyAttack.isDown();
        if (l && !leftDown) {
            LEFT.addLast(now);
            lastLeftClickAt = now;
        }
        leftDown = l;

        boolean r = mc.options.keyUse.isDown();
        if (r && !rightDown) {
            RIGHT.addLast(now);
        }
        rightDown = r;

        prune(LEFT, now);
        prune(RIGHT, now);
    }

    private static void prune(ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) {
            q.removeFirst();
        }
    }

    public static int left() {
        return LEFT.size();
    }

    public static int right() {
        return RIGHT.size();
    }

    public static long attackAgeMillis() {
        return lastLeftClickAt == 0 ? Long.MAX_VALUE : System.currentTimeMillis() - lastLeftClickAt;
    }

    private ClickTracker() {
    }
}
