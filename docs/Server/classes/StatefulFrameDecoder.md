# StatefulFrameDecoder

Package `org.apollo.util`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/StatefulFrameDecoder.java`](2006Scape Server/src/main/java/org/apollo/util/StatefulFrameDecoder.java).

A stateful implementation of a {@link ByteToMessageDecoder} which may be extended and used by other classes. The current state is tracked by this class and is a user-specified enumeration.  The state may be changed by calling the {@link StatefulFrameDecoder#setState} method.  The current state is supplied as a parameter in the {@link StatefulFrameDecoder#decode} and {@link StatefulFrameDecoder#decodeLast} methods.  This class is not thread safe: it is recommended that the state is only set in the decode methods overridden.  @author Graham @param <T> The state enumeration.

```java
* current state is tracked by this class and is a user-specified enumeration.
public StatefulFrameDecoder(T state)
public final void setState(T state)
protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, T state) throws Exception;
```
