# RequestWorkerPool

Package `org.apollo.jagcached`.

Defined in [`2006Scape Server/src/main/java/org/apollo/jagcached/RequestWorkerPool.java`](2006Scape Server/src/main/java/org/apollo/jagcached/RequestWorkerPool.java).

A class which manages the pool of request workers. @author Graham @author Ryley Kimmel <ryley.kimmel@live.com>

```java
* A class which manages the pool of request workers.
private static final int THREADS_PER_REQUEST_TYPE = Runtime.getRuntime().availableProcessors();
private final List<RequestWorker<?, ?>> workers = new ArrayList<RequestWorker<?, ?>>();
public static UpdateDispatcher getDispatcher()
public RequestWorkerPool()
private static final UpdateDispatcher dispatcher = new UpdateDispatcher();
```
