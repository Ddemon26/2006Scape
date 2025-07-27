# game.definitions.EntityDef

Defined in [`2006Scape Client/src/main/java/game.definitions.EntityDef.java`](2006Scape Client/src/main/java/game.definitions.EntityDef.java).

game.entities.Entity Def helper class.

```java
public final class game.definitions.EntityDef {
public static game.definitions.EntityDef forID(int i)
public render.geometry.Model getModel()
public game.definitions.EntityDef transform()
public static void unpackConfig(core.network.StreamLoader streamLoader)
public static void nullLoader()
public render.geometry.Model getAnimatedModel(int primaryFrame, int secondaryFrame, int[] frameData)
public static util.collections.MRUCache mruNodes = new util.collections.MRUCache(30);
```
