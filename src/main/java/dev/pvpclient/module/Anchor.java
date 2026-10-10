package dev.pvpclient.module;

/** Screen corner/edge a module is stacked against. */
public enum Anchor {
    TOP_LEFT("Top Left", false, 0),
    TOP_RIGHT("Top Right", true, 0),
    MIDDLE_LEFT("Middle Left", false, 1),
    MIDDLE_RIGHT("Middle Right", true, 1),
    BOTTOM_LEFT("Bottom Left", false, 2),
    BOTTOM_RIGHT("Bottom Right", true, 2);

    public final String label;
    /** true = right aligned. */
    public final boolean right;
    /** 0 = top, 1 = middle, 2 = bottom. */
    public final int vertical;

    Anchor(String label, boolean right, int vertical) {
        this.label = label;
        this.right = right;
        this.vertical = vertical;
    }

    public Anchor next() {
        Anchor[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public static Anchor byName(String name, Anchor fallback) {
        if (name != null) {
            for (Anchor a : values()) {
                if (a.name().equals(name)) {
                    return a;
                }
            }
        }
        return fallback;
    }
}
