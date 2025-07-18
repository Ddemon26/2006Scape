# IndexedFileSystem

Package `org.apollo.cache`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/IndexedFileSystem.java`](2006Scape Server/src/main/java/org/apollo/cache/IndexedFileSystem.java).

A file system based on top of the operating system's file system. It consists of a data file and index files. Index files point to blocks in the data file, which contains the actual data.  @author Graham

```java
public final class IndexedFileSystem implements Closeable {
private final Map<FileDescriptor, Archive> cache = new HashMap<>(FileSystemConstants.ARCHIVE_COUNT);
public IndexedFileSystem(Path base, boolean readOnly) throws FileNotFoundException
public void close() throws IOException
public Archive getArchive(int type, int file) throws IOException
public ByteBuffer getCrcTable() throws IOException
```
