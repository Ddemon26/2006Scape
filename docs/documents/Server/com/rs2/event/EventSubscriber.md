# EventSubscriber

Package `com.rs2.event`.

Defined in [`2006Scape Server/src/main/java/com/rs2/event/EventSubscriber.java`](2006Scape Server/src/main/java/com/rs2/event/EventSubscriber.java).

Represents a single subscriber for some {@link Event}.  @author Ryley Kimmel <ryley.kimmel@live.com>  @param <E> The type of event to the subscriber.

```java
public interface EventSubscriber<E extends Event> extends Predicate<E> {
```
