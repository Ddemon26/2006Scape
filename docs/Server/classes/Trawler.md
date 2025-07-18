# Trawler

Package `com.rs2.game.content.minigames.trawler`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/minigames/trawler/Trawler.java`](2006Scape Server/src/main/java/com/rs2/game/content/minigames/trawler/Trawler.java).

Trawler helper class.

```java
public class Trawler extends GroupMinigame {
public WaitingRoom waiting_room = new TrawlerWaitingRoom(this);
public ArrayList<Player> players = new ArrayList<Player>();
public ArrayList<Player> players_to_remove = new ArrayList<Player>();
private final Random random_gen = new Random();
public static int getIndex(int x, int y)
```
