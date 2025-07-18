# Player

Represents a logged in user within the game world. The abstract `Player` class holds
core state such as inventories, skills and movement queues. The concrete `Client`
class extends it to handle packet I/O.

Key responsibilities include:

- Managing helper components like `PlayerAssistant`, `CombatAssistant` and
  various skill handlers.
- Processing queued packets and updating the world each server tick.
- Handling logout, death and other gameplay related events.

Source: [Player.java](../../2006Scape%20Server/src/main/java/com/rs2/game/players/Player.java)

```java
Player p = new Client(session);
p.getPlayerAssistant().movePlayer(3222, 3218, 0);
p.update();
```
