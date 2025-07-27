# BotHandler

Package `com.rs2.game.bots`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/bots/BotHandler.java`](2006Scape Server/src/main/java/com/rs2/game/bots/BotHandler.java).

Handles bot related functionality.

```java
public class BotHandler {
public static Bot connectBot(String username, Integer x, Integer y, Integer z)
public static void loadPlayerShops()
public static void playerShop(game.entities.Player player)
public static void closeShop(game.entities.Player player)
public static void addCoins(int shop_id, int amount)
public static int checkCoins(game.entities.Player player)
public static void takeCoins(game.entities.Player player)
public static void addTobank(int shop_id, int item_id, int amount)
public static void removeFrombank(int shop_id, int item_id, int amount)
public static int getItemPrice(int shop_id, int item_id)
public static void setPrice(int shop_id, int item_id, int amount)
```
