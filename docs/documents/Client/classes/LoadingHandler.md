# LoadingHandler

Defined in [`2006Scape Client/src/main/java/core/handlers/LoadingHandler.java`](../../2006Scape%20Client/src/main/java/core/handlers/LoadingHandler.java).

Handles client loading stages and CRC connection logic extracted from [`Game`](Game.md).

```java
final class LoadingHandler {
    LoadingHandler(Game game)
    void loadingStages()
    int checkMapLoadStatus()
    void connectServer()
    DataInputStream openJagGrabInputStream(String s) throws IOException
    StreamLoader streamLoaderForName(int i, String s, String s1, int j, int k)
}
```
