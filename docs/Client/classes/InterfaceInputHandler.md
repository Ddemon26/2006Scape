# InterfaceInputHandler

Defined in [`2006Scape Client/src/main/java/core/handlers/InterfaceInputHandler.java`](../../2006Scape%20Client/src/main/java/core/handlers/InterfaceInputHandler.java).

Handles interface-related input tasks extracted from [`Game`](Game.md).

```java
final class InterfaceInputHandler {
    InterfaceInputHandler(Game game)
    boolean promptUserForInput(RSInterface widget)
    void handleScrollbarInput(int i, int j, int k, int l,
                              RSInterface scrollInterface,
                              int i1, boolean flag, int j1)
    void resetInterfaceAnimation(int i)
    void openInterface(int interfaceID)
    void openSideInterface(int tab, int interfaceID)
}
```
