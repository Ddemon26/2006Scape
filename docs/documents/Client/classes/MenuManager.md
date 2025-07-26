# MenuManager

Defined in [`2006Scape Client/src/main/java/core/MenuManager.java`](../../2006Scape%20Client/src/main/java/core/MenuManager.java).

Handles menu interaction logic extracted from [`Game`](Game.md).

```java
final class MenuManager {
    MenuManager(Game game)
    void sendFrame126(String text, int id)
    boolean menuHasAddFriend(int index)
    boolean processMenuClick()
    void drawMenu()
    void determineMenuSize()
    void build3dScreenMenu()
    void buildAtNPCMenu(EntityDef def, int id, int x, int y)
    void buildAtPlayerMenu(int x, int id, Player player, int y)
}
```
