# PlayerHandler

Maintains the global player list, assigning slots to new connections and
processing each active player every tick. `PlayerHandler` updates player
counters and saves data on logout.

Source: [PlayerHandler.java](../../2006Scape%20Server/src/main/java/com/rs2/game/players/PlayerHandler.java)

```java
// called from GameEngine each server tick
playerHandler.process();
```
