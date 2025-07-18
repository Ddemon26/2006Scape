# NameUtil

Package `org.apollo.util`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/NameUtil.java`](2006Scape Server/src/main/java/org/apollo/util/NameUtil.java).

Contains name-related utility methods.  @author Graham

```java
public final class NameUtil {
private static final long FIRST_VALID_NAME = encodeBase37("");
private static final long LAST_VALID_NAME = encodeBase37("999999999999");
public static String decodeBase37(long value)
public static long encodeBase37(String string)
private NameUtil()
```
