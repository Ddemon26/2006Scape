import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;

public class EntityHitDataTest {
    @Test public void argumentsIndependentlySelectMarkerNumberAndExpiry() {
        for (int j : new int[] {0, 1, 2, 5}) {
            for (int k : new int[] {0, 1, 19, 255}) {
                for (int l : new int[] {0, 200, 10000}) {
                    Entity e = new Entity();
                    e.updateHitData(j, k, l);
                    assertEquals(j, e.hitMarkTypes[0]);
                    assertEquals(k, e.hitArray[0]);
                    assertEquals(l + 70, e.hitsLoopCycle[0]);
                    assertArrayEquals(new int[] {k, 0, 0, 0}, e.hitArray);
                }
            }
        }
    }

    @Test public void eachOfFourPositionsCanBeTheOnlyReusablePosition() {
        for (int i1 = 0; i1 < 4; i1++) {
            Entity e = occupied();
            e.hitsLoopCycle[i1] = 200;
            int[] a = e.hitArray.clone(), b = e.hitMarkTypes.clone();
            int[] c = e.hitsLoopCycle.clone();
            e.updateHitData(2, 37, 200);
            a[i1] = 37; b[i1] = 2; c[i1] = 270;
            assertArrayEquals(a, e.hitArray);
            assertArrayEquals(b, e.hitMarkTypes);
            assertArrayEquals(c, e.hitsLoopCycle);
        }
    }

    @Test public void equalityReusesButOneTickInFutureDoesNot() {
        Entity e = occupied();
        e.hitsLoopCycle[0] = 201;
        e.hitsLoopCycle[1] = 200;
        e.updateHitData(1, 9, 200);
        assertArrayEquals(new int[] {10, 9, 12, 13}, e.hitArray);
        assertArrayEquals(new int[] {201, 270, 500, 500}, e.hitsLoopCycle);
    }

    @Test public void firstReusablePositionWinsRatherThanOldestExpiry() {
        Entity e = occupied();
        System.arraycopy(new int[] {199, 100, 0, 200}, 0, e.hitsLoopCycle, 0, 4);
        e.updateHitData(2, 29, 200);
        assertArrayEquals(new int[] {29, 11, 12, 13}, e.hitArray);
        assertArrayEquals(new int[] {270, 100, 0, 200}, e.hitsLoopCycle);
    }

    @Test public void fullCapacityDiscardsWithoutChangingAnyStoredValue() {
        Entity e = new Entity();
        for (int i1 = 0; i1 < 4; i1++) e.updateHitData(i1, 11 + i1, 200);
        int[] a = e.hitArray.clone(), b = e.hitMarkTypes.clone();
        int[] c = e.hitsLoopCycle.clone();
        e.updateHitData(5, 99, 269);
        assertArrayEquals(a, e.hitArray);
        assertArrayEquals(b, e.hitMarkTypes);
        assertArrayEquals(c, e.hitsLoopCycle);
        e.updateHitData(5, 99, 270);
        assertArrayEquals(new int[] {99, 12, 13, 14}, e.hitArray);
        assertArrayEquals(new int[] {340, 270, 270, 270}, e.hitsLoopCycle);
    }

    @Test public void expiredLaterPositionsAreSkippedAfterFirstWrite() {
        Entity e = new Entity();
        e.updateHitData(1, 255, 0);
        assertArrayEquals(new int[] {255, 0, 0, 0}, e.hitArray);
        assertArrayEquals(new int[] {70, 0, 0, 0}, e.hitsLoopCycle);
        e.updateHitData(0, 0, 69);
        assertArrayEquals(new int[] {70, 139, 0, 0}, e.hitsLoopCycle);
        e.updateHitData(2, 17, 70);
        assertArrayEquals(new int[] {17, 0, 0, 0}, e.hitArray);
        assertArrayEquals(new int[] {140, 139, 0, 0}, e.hitsLoopCycle);
    }

    private Entity occupied() {
        Entity e = new Entity();
        for (int i1 = 0; i1 < 4; i1++) {
            e.hitArray[i1] = 10 + i1;
            e.hitMarkTypes[i1] = 20 + i1;
        }
        Arrays.fill(e.hitsLoopCycle, 500);
        return e;
    }
}
