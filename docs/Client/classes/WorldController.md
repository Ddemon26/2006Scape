# WorldController

Defined in [`2006Scape Client/src/main/java/WorldController.java`](2006Scape Client/src/main/java/WorldController.java).

World Controller helper class.

```java
final class WorldController {
public WorldController(int[][][] heights)
public static void nullLoader()
public void initToNull()
public void setActivePlane(int i)
public void shiftDownPlanes(int i, int j)
public static void addCullingCluster(int i, int j, int k, int l, int i1, int j1, int l1, int i2)
public void setGroundFlag(int i, int j, int k, int l)
public void addTile(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2, int i3, int j3, int k3, int l3, int i4, int j4, int k4, int l4)
public void addTileDecoration(int plane, int height, int tileY, Animable renderable, byte config, int uid, int tileX)
public void addItemPile(int i, int j, Animable renderable, int k, Animable secondaryRenderable, Animable topRenderable, int l, int i1)
public void addBoundaryObject(int i, Animable renderable, int j, int k, byte byte0, int l, Animable secondaryRenderable, int i1, int j1, int k1)
public void addWallDecoration(int i, int j, int k, int i1, int j1, int k1, Animable renderable, int l1, byte byte0, int i2, int j2)
public boolean addGameObject(int i, byte byte0, int j, int k, Animable renderable, int l, int i1, int j1, int k1, int l1)
public boolean addAnimableObject(int i, int j, int k, int l, int i1, int j1, int k1, Animable renderable, boolean flag)
public boolean addAnimatingObject(int j, int k, Animable renderable, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2)
public void clearObj5Cache()
public void updateWallDecorationPosition(int i, int k, int l, int i1)
public void clearBoundaryObject(int i, int j, int k, byte byte0)
public void clearWallDecoration(int j, int k, int l)
public void removeSceneObject(int i, int k, int l)
public void clearTileDecoration(int i, int j, int k)
public void clearItemPile(int i, int j, int k)
public BoundaryObject getBoundaryObject(int i, int j, int k)
public WallDecoration getWallDecoration(int i, int k, int l)
public SceneObject getSceneObject(int i, int j, int k)
public TileDecoration getTileDecoration(int i, int j, int k)
public int getBoundaryObjectUid(int i, int j, int k)
public int getWallDecorationUid(int i, int j, int l)
public int getSceneObjectUid(int i, int j, int k)
public int getTileDecorationUid(int i, int j, int k)
public int getObjectConfig(int i, int j, int k, int l)
public void applySceneLighting(int i, int k, int i1)
public void renderMinimapTile(int ai[], int i, int k, int l, int i1)
public static void buildVisibilityMap(int i, int j, int k, int l, int ai[])
public void queueClick(int i, int j)
public void renderScene(int i, int j, int k, int l, int i1, int j1)
```
