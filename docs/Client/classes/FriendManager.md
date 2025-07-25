# FriendManager

Defined in [`2006Scape Client/src/main/java/core/FriendManager.java`](../../2006Scape%20Client/src/main/java/core/FriendManager.java).

Handles friend list operations extracted from [`Game`](Game.md).

```java
final class FriendManager {
    FriendManager(Game game)
    void addFriend(long id)
    void delFriend(long id)
    boolean isFriendOrSelf(String name)
    void drawFriendsListOrWelcomeScreen(RSInterface component)
    boolean buildFriendsListMenu(RSInterface listInterface)
}
```
