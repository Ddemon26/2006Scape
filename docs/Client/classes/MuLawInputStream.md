# MuLawInputStream

Defined in [`2006Scape Client/src/main/java/audio/MuLawInputStream.java`](2006Scape Client/src/main/java/audio/MuLawInputStream.java).

Converts 16-bit PCM samples stored in an integer buffer to 8-bit mu-law encoded bytes.

```java
final class MuLawInputStream extends InputStream {
public final synchronized int read(byte[] is, int i, int i_4_)
public final int read()
```
