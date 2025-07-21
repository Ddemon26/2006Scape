# CacheUtils

Defined in [`2006Scape Client/src/main/java/core/CacheUtils.java`](2006Scape Client/src/main/java/core/CacheUtils.java).

Utility methods for reading and repacking cache files.

```java
public final class CacheUtils {
    public static String getFileNameWithoutExtension(String fileName)
    public static String indexLocation(int cacheIndex, int index)
    public static void repackCacheIndex(int cacheIndex, Decompressor[] decompressors)
    public static byte[] fileToByteArray(int cacheIndex, int index)
}
```
