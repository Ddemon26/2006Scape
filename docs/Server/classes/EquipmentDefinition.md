# EquipmentDefinition

Package `org.apollo.cache.def`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/def/EquipmentDefinition.java`](2006Scape Server/src/main/java/org/apollo/cache/def/EquipmentDefinition.java).

Represents a type of Item that may be equipped.  @author Graham

```java
public final class EquipmentDefinition {
private static final Map<Integer, EquipmentDefinition> definitions = new HashMap<>();
public static int count()
public static void init(EquipmentDefinition[] definitions)
public static EquipmentDefinition lookup(int id)
public EquipmentDefinition(int id)
```
