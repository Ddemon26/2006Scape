# CompressionUtil

Package `org.apollo.util`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/CompressionUtil.java`](2006Scape Server/src/main/java/org/apollo/util/CompressionUtil.java).

A utility class for performing compression/decompression.  @author Graham

```java
* A utility class for performing compression/decompression.
public static byte[] bzip2(byte[] uncompressed) throws IOException
public static void debzip2(byte[] compressed, byte[] decompressed) throws IOException
public static void degzip(byte[] compressed, byte[] decompressed) throws IOException
public static byte[] degzip(ByteBuffer compressed) throws IOException
public static byte[] gzip(byte[] uncompressed) throws IOException
```
