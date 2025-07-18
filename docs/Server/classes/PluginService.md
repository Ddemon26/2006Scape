# PluginService

Package `com.rs2.plugin`.

Defined in [`2006Scape Server/src/main/java/com/rs2/plugin/PluginService.java`](2006Scape Server/src/main/java/com/rs2/plugin/PluginService.java).

The service that services plugins.  @author Vult-R

```java
public final class PluginService {
private static final Logger logger = LoggerUtils.getLogger(PluginService.class);
private static final List<EventSubscriber<?>> subscribers = new ArrayList<>();
public void load()
private Collection<EventSubscriber<?>> findPlugins() throws IOException
private Collection<EventSubscriber<?>> findPlugins(File dir)
```
