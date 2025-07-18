# GameEngine

The main entry point for running the server. `GameEngine` bootstraps every
subsystem and runs the core game loop once every few hundred milliseconds.

Highlights:

- Loads configuration, caches and plugins on startup.
- Accepts network connections and exposes the Netty based pipeline.
- Schedules the primary tick which updates players, NPCs and objects.

Source: [GameEngine.java](../../2006Scape%20Server/src/main/java/com/rs2/GameEngine.java)

```java
public static void main(String[] args) throws IOException {
    System.out.println("Starting game engine..");
    FileServer fs = new FileServer();
    fs.start();
    scheduler.scheduleAtFixedRate(() -> playerHandler.process(),
            0, Constants.CYCLE_TIME, TimeUnit.MILLISECONDS);
}
```
