# UniversalEventProvider

Package `com.rs2.event`.

Defined in [`2006Scape Server/src/main/java/com/rs2/event/UniversalEventProvider.java`](2006Scape Server/src/main/java/com/rs2/event/UniversalEventProvider.java).

A universal event provider which posts, provides and deprives subscribers.  @author Ryley Kimmel <ryley.kimmel@live.com>

```java
public final class UniversalEventProvider implements EventProvider {
private final Multimap<Class<? extends Event>, EventSubscriber<? super Event>> events = ArrayListMultimap.create();
private final EventContext context = new UniversalEventContext();
public void provideSubscriber(EventSubscriber<?> subscriber)
public void depriveSubscriber(EventSubscriber<?> subscriber)
private void checkSubscriber(EventSubscriber<?> subscriber, Consumer<SubscribesTo> consumer)
```
