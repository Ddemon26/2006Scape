# Trading

Package `com.rs2.game.players`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/players/Trading.java`](2006Scape Server/src/main/java/com/rs2/game/players/Trading.java).

Trading helper class.

```java
public class Trading {
public Trading(Player player2)
public CopyOnWriteArrayList<GameItem> offeredItems = new CopyOnWriteArrayList<GameItem>();
public void requestTrade(int id)
public boolean isCloseTo(Client tradedPlayer)
public void openTrade()
public void resetTItems(int WriteFrame)
public boolean fromTrade(int itemID, int fromSlot, int amount)
public boolean tradeItem(int itemID, int fromSlot, int amount)
public void resetTrade()
public void declineTrade()
public void declineTrade(boolean tellOther)
public void resetOTItems(int WriteFrame)
public void confirmScreen()
public void giveItems()
public void execute(CycleEventContainer container)
public void stop()
```
