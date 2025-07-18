# ItemDef

Defined in [`2006Scape Client/src/main/java/ItemDef.java`](2006Scape Client/src/main/java/ItemDef.java).

Item Def helper class.

```java
public final class ItemDef {
public static void resetCache()
public boolean areDialogueModelsCached(int gender)
public static void unpackConfig(StreamLoader streamLoader)
public Model getDialogueModel(int gender)
public boolean areWearModelsCached(int gender)
public Model getWearModel(int gender)
public static ItemDef lookup(int i)
public static Sprite getSprite(int i, int j, int k)
public Model getModel(int amount)
public Model getInterfaceModel(int amount)
public static MRUCache modelCache = new MRUCache(50);
```
