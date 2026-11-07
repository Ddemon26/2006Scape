import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Controlled original-name observations of Model.getModel and Model.isModelLoaded. */
public class ModelLookupTest {
    private Object savedHeaders, savedFetcher;
    private final java.util.List<Integer> requests = new java.util.ArrayList<Integer>();

    @Before public void setup() throws Exception {
        savedHeaders = get("aClass21Array1661");
        savedFetcher = get("aOnDemandFetcherParent_1662");
        Model.method459(8, new OnDemandFetcherParent() {
            @Override public void method548(int i) { requests.add(i); }
        });
        Model.method460(null, 3);
    }

    @After public void restore() throws Exception {
        set("aClass21Array1661", savedHeaders);
        set("aOnDemandFetcherParent_1662", savedFetcher);
    }

    @Test public void method463ReportsLoadedModelsAndRequestsTheMissingOnes() {
        assertTrue(Model.isModelLoaded(3));
        assertTrue(requests.isEmpty());
        assertFalse(Model.isModelLoaded(5));
        assertEquals(java.util.Collections.singletonList(5), requests);
        assertFalse(Model.isModelLoaded(2));
        assertEquals(java.util.Arrays.asList(5, 2), requests);
        Model.method461(3);
        assertFalse(Model.isModelLoaded(3));
        assertEquals(3, (int) requests.get(2));
    }

    @Test public void method462BuildsAFreshModelForLoadedIdsAndRequestsMissingOnes() {
        Model first = Model.getModel(3);
        Model second = Model.getModel(3);
        assertNotNull(first);
        assertNotSame(first, second);
        assertTrue(requests.isEmpty());
        assertNull(Model.getModel(6));
        assertEquals(java.util.Collections.singletonList(6), requests);
    }

    @Test public void method462UsesTheIdToSelectTheHeader() throws Exception {
        Object[] headers = (Object[]) get("aClass21Array1661");
        Class21 big = new Class21();
        big.anInt369 = 4;
        big.aByteArray368 = new byte[64];
        headers[1] = big;
        Model small = Model.getModel(3);
        Model large = Model.getModel(1);
        assertEquals(0, small.anInt1626);
        assertEquals(4, large.anInt1626);
    }

    @Test public void method462AndMethod463ReturnNullOrFalseBeforeInitialisation() throws Exception {
        set("aClass21Array1661", null);
        assertNull(Model.getModel(1));
        assertFalse(Model.isModelLoaded(1));
        assertTrue(requests.isEmpty());
    }

    private static Object get(String name) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true); return f.get(null);
    }
    private static void set(String name, Object v) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true); f.set(null, v);
    }
}
