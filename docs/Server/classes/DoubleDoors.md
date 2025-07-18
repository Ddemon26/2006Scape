# DoubleDoors

Package `com.rs2.game.globalworldobjects`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/globalworldobjects/DoubleDoors.java`](2006Scape Server/src/main/java/com/rs2/game/globalworldobjects/DoubleDoors.java).

@author Killamess

```java
public class DoubleDoors {
private final List<DoubleDoors> doors = new ArrayList<>();
public static DoubleDoors getSingleton()
private DoubleDoors(String file)
private DoubleDoors getDoor(int id, int x, int y, int z)
public boolean handleDoor(Player player, int id, int x, int y, int z)
```
