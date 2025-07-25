# Model

Defined in [`2006Scape Client/src/main/java/Model.java`](2006Scape Client/src/main/java/Model.java).

Model helper class.

```java
public final class Model extends Animable {
public static void clearCache()
public static void init(int i, OnDemandFetcherParent onDemandFetcherParent)
public static void loadModelData(byte abyte0[], int j)
public static void unload(int j)
public static Model create(int j)
public static boolean isLoaded(int i)
public Model(int i, Model models[])
public Model(Model models[])
public Model(boolean flag, boolean flag1, boolean flag2, Model model)
public Model(boolean flag, boolean flag1, Model model)
public void copyFromModel(Model src, boolean shareColor)
public void calculateBounds()
public void calculateBoundsY()
public void buildVertexGroups()
public void applyFrame(int i)
public void applyFrames(int ai[], int j, int k)
public void calculateNormals()
public void rotateX(int i)
public void translate(int i, int j, int l)
public void recolor(int i, int j)
public void mirror()
public void scaleModel(int i, int j, int l)
public void applyLighting(int i, int j, int k, int l, int i1, boolean flag)
public void applyShading(int i, int j, int k, int l, int i1)
public void transformVertices(int j, int k, int l, int i1, int j1, int k1)
public void render(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2)
public static final Model placeholderModel = new Model();
```
