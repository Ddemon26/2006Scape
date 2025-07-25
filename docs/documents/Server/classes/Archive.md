# Archive

Package `org.apollo.cache.archive`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/archive/Archive.java`](2006Scape Server/src/main/java/org/apollo/cache/archive/Archive.java).

Represents an archive.  @author Graham

```java
public final class Archive {
public static Archive decode(ByteBuffer buffer) throws IOException
public Archive(ArchiveEntry[] entries)
public ArchiveEntry getEntry(String name) throws FileNotFoundException
public static int hash(String name)
```
