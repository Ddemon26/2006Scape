package core;

/** Utility helpers extracted from {@link Game}. */
public final class GameUtils {
    private GameUtils() {}

    /**
     * Returns a random integer between 0 and the provided range inclusive.
     */
    public static int random(final float range) {
        return (int) (Math.random() * (range + 1));
    }

    /**
     * Formats the given integer with commas and a short suffix.
     */
    public static String intToKOrMilLongName(int i) {
        String s = String.valueOf(i);
        for (int k = s.length() - 3; k > 0; k -= 3) {
            s = s.substring(0, k) + "," + s.substring(k);
        }
        if (s.length() > 8) {
            s = "@gre@" + s.substring(0, s.length() - 8) + " million @whi@(" + s + ")";
        } else if (s.length() > 4) {
            s = "@cya@" + s.substring(0, s.length() - 4) + "K @whi@(" + s + ")";
        }
        return " " + s;
    }
}
