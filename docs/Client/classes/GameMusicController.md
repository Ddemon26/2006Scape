# GameMusicController

Defined in [`2006Scape Client/src/main/java/core/GameMusicController.java`](../../2006Scape%20Client/src/main/java/core/GameMusicController.java).

Handles music playback logic extracted from [`Game`](Game.md).

```java
public final class GameMusicController {
    GameMusicController(Game game)
    void musics()
    byte[] getMusic(int index)
    void queueSong(int delay, int volume, boolean bool, int music)
    void playSong(int volume, boolean bool, int music)
}
```
