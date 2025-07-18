# CycleEventHandler

Package `com.rs2.event`.

Defined in [`2006Scape Server/src/main/java/com/rs2/event/CycleEventHandler.java`](2006Scape Server/src/main/java/com/rs2/event/CycleEventHandler.java).

Handles all of our cycle based events  @author Stuart <RogueX> @author Null++

```java
public class CycleEventHandler {
public static CycleEventHandler getSingleton()
public CycleEventHandler()
public CycleEventContainer addEvent(int id, Object owner, CycleEvent event, int cycles)
public CycleEventContainer addEvent(Object owner, CycleEvent event, int cycles)
public void process()
public int getEventsCount()
public void stopEvents(Object owner)
public void stopEvents(Object owner, int id)
public void stopEvents(int id)
```
