# GodBooks

Package `com.rs2.game.items.impl`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/items/impl/GodBooks.java`](2006Scape Server/src/main/java/com/rs2/game/items/impl/GodBooks.java).

Handles the preaching of god books @author Final Project

```java
public enum GodBooks {
private GodBooks(int itemId, String[][] preachData)
private static Map<Integer, GodBooks> godBooks = new HashMap<Integer, GodBooks>();
public static void sendPreachOptions(Player player, int itemId)
public static void handlePreach(Player player, int itemId, int actionButtonId)
public void execute(CycleEventContainer container)
```
