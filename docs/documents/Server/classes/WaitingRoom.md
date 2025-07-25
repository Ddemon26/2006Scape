# WaitingRoom

Package `com.rs2.game.content.minigames.trawler`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/minigames/trawler/WaitingRoom.java`](2006Scape Server/src/main/java/com/rs2/game/content/minigames/trawler/WaitingRoom.java).

Waiting Room helper class.

```java
public abstract class WaitingRoom {
public ArrayList<Player> waiting = new ArrayList<Player>();
public abstract Boundary getLocation();
public abstract boolean startGame();
public abstract void onStart();
public abstract void onLeave(Player player);
public abstract void onJoin(Player p);
public abstract void onTimeChange();
public abstract boolean canStart();
public WaitingRoom(int minutes, int minimum)
public void join(Player player)
public void leave(Player player)
public void reset()
public void startWaiting()
public void execute(CycleEventContainer container)
public void stop()
public void messageWaiting(String message)
public boolean isActive()
public void setActive(boolean active)
public int getTimeRemaining()
```
