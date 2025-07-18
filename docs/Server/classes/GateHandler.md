# GateHandler

Package `com.rs2.game.globalworldobjects`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/globalworldobjects/GateHandler.java`](2006Scape Server/src/main/java/com/rs2/game/globalworldobjects/GateHandler.java).

GateHandler (needs to be re written tbh) @author Andrew (Mr Extremez)

```java
public class GateHandler {
public boolean isGate(int objectId)
public void spawnGate(Player player, int objectId, int newObjectX, int newObjectY, int height, int face)
public void openSingleGate(Player player, int objectId, int newObjectX, int newObjectY, int oldObjectX, int oldObjectY, int walkX, int walkY, int newFace, int oldFace)
public void openMetalGateWalk(Player player, int objectId, int objectId2, int newObjectX, int newObjectY, int newObjectX2, int newObjectY2, int oldObjectX, int oldObjectY, int oldObjectX2, int oldObjectY2, int walkX, int walkY, int newFace, int newFace2, int oldFace)
public void handleWoodenGate(Player player, int objectId, int objectId2, int newObjectX, int newObjectY, int newObjectX2, int newObjectY2, int oldObjectX, int oldObjectY, int oldObjectX2, int oldObjectY2, int type)
public void handleMetalGate(Player player, int objectId, int objectId2, int newObjectX, int newObjectY, int newObjectX2, int newObjectY2, int oldObjectX, int oldObjectY, int oldObjectX2, int oldObjectY2, int type)
```
