# ShopHandler

Package `com.rs2.game.shops`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/shops/ShopHandler.java`](2006Scape Server/src/main/java/com/rs2/game/shops/ShopHandler.java).

Handles shop related functionality.

```java
public class ShopHandler {
public ShopHandler()
public static int restockTimeItem(int itemId)
public void process()
public void loadShops()
public boolean writeShops(String FileName)
public static void createPlayerShop(Client player)
public static void closePlayerShop(Client player)
public static void refreshshop(int shop_id)
public static int getStock(int shop_id, int item_id)
public static void buyItem(int shop_id, int item_id, int amount)
public static boolean playerOwnsStore(int shop_id, Player player)
```
