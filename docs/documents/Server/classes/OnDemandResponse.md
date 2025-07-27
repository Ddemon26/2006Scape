# OnDemandResponse

Package `org.apollo.net.codec.update`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/codec/update/OnDemandResponse.java`](2006Scape Server/src/main/java/org/apollo/net/codec/update/OnDemandResponse.java).

Represents a single 'on-demand' response.  @author Graham

```java
public final class OnDemandResponse {
public OnDemandResponse(FileDescriptor fileDescriptor, int fileSize, int chunkId, ByteBuf chunkData)
public ByteBuf getChunkData()
public int getChunkId()
public FileDescriptor getFileDescriptor()
public int getFileSize()
```
