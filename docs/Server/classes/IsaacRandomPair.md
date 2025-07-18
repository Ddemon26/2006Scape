# IsaacRandomPair

Package `org.apollo.util.security`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/security/IsaacRandomPair.java`](2006Scape Server/src/main/java/org/apollo/util/security/IsaacRandomPair.java).

A pair of two {@link IsaacRandom} random number generators used as a stream cipher. One takes the role of an encoder for this endpoint, the other takes the role of a decoder for this endpoint.  @author Graham

```java
public final class IsaacRandomPair {
public IsaacRandomPair(IsaacRandom encodingRandom, IsaacRandom decodingRandom)
public IsaacRandom getDecodingRandom()
public IsaacRandom getEncodingRandom()
```
