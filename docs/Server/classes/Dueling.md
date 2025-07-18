# Dueling

Package `com.rs2.game.content.minigames`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/minigames/Dueling.java`](2006Scape Server/src/main/java/com/rs2/game/content/minigames/Dueling.java).

Dueling helper class.

```java
public class Dueling {
public Dueling(Player player2)
public CopyOnWriteArrayList<GameItem> otherStakedItems = new CopyOnWriteArrayList<GameItem>();
public CopyOnWriteArrayList<GameItem> stakedItems = new CopyOnWriteArrayList<GameItem>();
public void requestDuel(int id)
public void openDuel()
public void sendDuelEquipment(int itemId, int amount, int slot)
public void refreshduelRules()
public void refreshDuelScreen()
public boolean stakeItem(int itemID, int fromSlot, int amount)
public boolean fromDuel(int itemID, int fromSlot, int amount)
public void confirmDuel()
public void startDuel()
public static void handleForfeit(Player player)
public void duelVictory()
public void duelRewardInterface()
public void claimStakedItems()
public void declineDuel()
public void checkDuelWalk()
public void resetDuel()
public void resetDuelItems()
public void changeDuelStuff()
public void selectRule(int i) { // rules
```
