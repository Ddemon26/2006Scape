package util;

import org.junit.Test;
import render.geometry.Model;
import render.geometry.ModelHeader;
import util.configuration.IDK;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class IDKTest {
    @Test
    public void testReadyReturnsTrueWhenModelsLoaded() throws Exception {
        IDK idk = new IDK();
        Field modelIdsField = IDK.class.getDeclaredField("modelIds");
        modelIdsField.setAccessible(true);
        modelIdsField.set(idk, new int[] {0, 1});

        ModelHeader[] headers = { new ModelHeader(), new ModelHeader() };
        Field cacheField = Model.class.getDeclaredField("modelHeaderCache");
        cacheField.setAccessible(true);
        cacheField.set(null, headers);

        assertTrue(idk.ready());
    }

    @Test
    public void testHeadLoadedFalseWhenMissingModel() throws Exception {
        IDK idk = new IDK();
        Field headField = IDK.class.getDeclaredField("headModelIds");
        headField.setAccessible(true);
        int[] ids = (int[]) headField.get(idk);
        ids[0] = 0;

        ModelHeader[] headers = { null };
        Field cacheField = Model.class.getDeclaredField("modelHeaderCache");
        cacheField.setAccessible(true);
        cacheField.set(null, headers);

        assertFalse(idk.headLoaded());
    }

    @Test
    public void testGetBodyModelReturnsNullWhenNoModels() {
        IDK idk = new IDK();
        assertNull(idk.getBodyModel());
    }
}
