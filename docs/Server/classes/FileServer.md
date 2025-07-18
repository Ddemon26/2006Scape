# FileServer

Package `org.apollo.jagcached`.

Defined in [`2006Scape Server/src/main/java/org/apollo/jagcached/FileServer.java`](2006Scape Server/src/main/java/org/apollo/jagcached/FileServer.java).

The core class of the file server. @author Graham

```java
* The core class of the file server.
private final EventLoopGroup loopGroup = new NioEventLoopGroup();
private final ServerBootstrap serviceBootstrap = new ServerBootstrap();
private static final Logger logger = Logger.getLogger(FileServer.class.getName());
public SocketAddress service = new InetSocketAddress((Constants.WORLD == 1) ? 43594 : 43596 + Constants.WORLD);
public void start() throws Exception
```
