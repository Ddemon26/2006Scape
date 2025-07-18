# ItemDefinition

Package `org.apollo.cache.def`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/def/ItemDefinition.java`](2006Scape Server/src/main/java/org/apollo/cache/def/ItemDefinition.java).

Represents a type of Item.  @author Graham

```java
public final class ItemDefinition {
private static final BiMap<Integer, Integer> notes = HashBiMap.create();
private static final BiMap<Integer, Integer> notesInverse = notes.inverse();
public static int count()
public static ItemDefinition[] getDefinitions()
public static void init(ItemDefinition[] definitions)
```
