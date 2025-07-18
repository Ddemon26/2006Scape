# LoginDecoder

Package `org.apollo.net.codec.login`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/codec/login/LoginDecoder.java`](2006Scape Server/src/main/java/org/apollo/net/codec/login/LoginDecoder.java).

A {@link StatefulFrameDecoder} which decodes the login request frames.  @author Graham

```java
public final class LoginDecoder extends StatefulFrameDecoder<LoginDecoderState> {
private static final Logger logger = Logger.getLogger(LoginDecoder.class.getName());
private static final SecureRandom RANDOM = new SecureRandom();
public LoginDecoder()
protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, LoginDecoderState state)
private void decodeHandshake(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
```
