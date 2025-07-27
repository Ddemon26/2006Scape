# game.definitions.ItemDef

Defined in [`2006Scape Client/src/main/java/game.definitions.ItemDef.java`](2006Scape Client/src/main/java/game.definitions.ItemDef.java).

game.items.Item Def helper class.

```java
public final class game.definitions.ItemDef {
public static void resetCache()
public boolean areDialogueModelsCached(int gender)
public static void unpackConfig(core.network.StreamLoader streamLoader)
public render.geometry.Model getDialogueModel(int gender)
public boolean areWearModelsCached(int gender)
public render.geometry.Model getWearModel(int gender)
public static game.definitions.ItemDef lookup(int i)
public static render.core.Sprite getSprite(int i, int j, int k)
public render.geometry.Model getModel(int amount)
public render.geometry.Model getInterfaceModel(int amount)
public static util.collections.MRUCache modelCache = new util.collections.MRUCache(50);
```
