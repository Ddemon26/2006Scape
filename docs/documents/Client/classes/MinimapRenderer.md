# MinimapRenderer

Defined in [`2006Scape Client/src/main/java/core/MinimapRenderer.java`](../../2006Scape%20Client/src/main/java/core/MinimapRenderer.java).

Generates and draws the game's minimap, extracted from [`Game`](Game.md).

```java
final class MinimapRenderer {
    MinimapRenderer(Game game)
    void generateMinimap(int plane)
    void drawMinimap()
    void markMinimap(Sprite sprite, int dx, int dy)
    void npcScreenPos(Entity entity, int height)
    void calcEntityScreenPos(int x, int z, int y)
    void processMinimapActions()
    void processMainScreenClick()
    void drawMinimapHint(Sprite sprite, int y, int x)
}
```
