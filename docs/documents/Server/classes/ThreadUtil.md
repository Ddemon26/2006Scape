# ThreadUtil

Package `org.apollo.util`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/ThreadUtil.java`](2006Scape Server/src/main/java/org/apollo/util/ThreadUtil.java).

A static utility class which provides ease of use functionality for {@link Thread}s  @author Ryley @author Major

```java
* A static utility class which provides ease of use functionality for {@link Thread}s
public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
public static ThreadFactory create(String name)
public static ThreadFactory create(String name, int priority)
public static ThreadFactory create(String name, int priority, UncaughtExceptionHandler handler)
```
