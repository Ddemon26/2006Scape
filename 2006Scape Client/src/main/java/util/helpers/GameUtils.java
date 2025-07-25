package util.helpers;

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

    /**
     * Formats numbers to K/M format with basic thresholds.
     */
    public static String intToKOrMil(int j) {
        if (j < 0x186a0) {
            return String.valueOf(j);
        }
        if (j < 0x989680) {
            return j / 1000 + "K";
        } else {
            return j / 0xf4240 + "M";
        }
    }

    /**
     * Formats numbers with decimal precision and letter suffixes (K, M, B).
     */
    public static String intToShortLetter(long number) {
        java.text.DecimalFormat nf = new java.text.DecimalFormat("0.0");
        double i = number;
        if (i >= 1e9) { // 1B
            return nf.format((i / 1e9)) + "B";
        }
        if (i >= 1e7) { // 1K
            return (int) (i / 1e6) + "M";
        }
        if (i >= 1e6) { // 1M
            return nf.format((i / 1e6)) + "M";
        }
        if (i >= 1e4) { // 1K
            return (int) (i / 1e3) + "K";
        }
        if (i >= 1e3) { // 1K
            return nf.format((i / 1e3)) + "K";
        }
        return "" + number;
    }
}
