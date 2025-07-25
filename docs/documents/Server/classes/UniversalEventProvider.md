# UniversalEventProvider

Package `com.rs2.event`.

Defined in [`2006Scape Server/src/main/java/com/rs2/event/UniversalEventProvider.java`](2006Scape Server/src/main/java/com/rs2/event/UniversalEventProvider.java).

A universal event provider which posts, provides and deprives subscribers.  @author Ryley Kimmel <ryley.kimmel@live.com>

```java
public final class UniversalEventProvider implements EventProvider {
public void provideSubscriber(EventSubscriber<?> subscriber)
public void depriveSubscriber(EventSubscriber<?> subscriber)
public <E extends Event> void post(Player player, E event)
public Multimap<Class<? extends Event>, EventSubscriber<? super Event>> getEvents()
```
