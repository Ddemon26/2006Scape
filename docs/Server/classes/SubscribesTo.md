# SubscribesTo

Package `com.rs2.event`.

Defined in [`2006Scape Server/src/main/java/com/rs2/event/SubscribesTo.java`](2006Scape Server/src/main/java/com/rs2/event/SubscribesTo.java).

Indicates that types annotated are an {@link EventSubscriber} and subscribe to one specific {@link Event}. {@link #value()} enforces that the specified event value is indeed an event. All event subscribers MUST be annotated otherwise {@link EventProvider}s will be unable to provide and deprive subscribers.  @author Ryley Kimmel <ryley.kimmel@live.com>

```java
public @interface SubscribesTo {
```
