package core;

/**
 * Utility for computing total experience and level sums.
 */
public final class PlayerStatsCalculator {
    private PlayerStatsCalculator() {}

    public static long calculateTotalExp(int[] currentExp) {
        long exp = 0;
        for (int value : currentExp) {
            exp += value;
        }
        return exp;
    }

    public static int calculateTotalLevels(int[] maxStats) {
        int levels = 0;
        for (int value : maxStats) {
            levels += value;
        }
        // need to remove 4 for some reason
        return levels - 4;
    }
}
