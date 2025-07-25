# Stream

**Package:** `com.rs2.util`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/util/Stream.java`](2006Scape Server/src/main/java/com/rs2/util/Stream.java)

## Overview

The `Stream` class is a specialized binary data serialization utility designed for the RuneScape network protocol. It provides efficient methods for writing and reading various data types to/from byte arrays, with support for different byte orders, bit-level operations, and packet encryption. This class is fundamental to all client-server communication, handling the serialization of game data into packets that can be transmitted over the network.

## Key Responsibilities

- **Binary Data Serialization**: Converting primitive data types to byte arrays
- **Packet Construction**: Building network packets with headers and variable sizes
- **Bit-Level Operations**: Efficient packing of small values using bit manipulation
- **Encryption Support**: Integration with ISAAC encryption for packet security
- **Memory Management**: Dynamic buffer resizing and efficient memory usage
- **Protocol Compliance**: Ensuring data is formatted according to RuneScape protocol

## Core Architecture

### Buffer Management
```java
public byte buffer[] = null;        // Main data buffer
public int currentOffset = 0;       // Current write/read position
public int bitPosition = 0;         // Bit-level position for bit operations
```

### Frame Stack (for Variable-Size Packets)
```java
private static final int frameStackSize = 10;
private int frameStackPtr = -1;
private final int frameStack[] = new int[frameStackSize];
```

### Encryption Support
```java
public IsaacRandom packetEncryption = null;  // ISAAC cipher for packet encryption
```

## Constructors

### `Stream()`
Creates an empty stream:
```java
public Stream() {
    // Creates stream with no initial buffer
}
```

### `Stream(byte[] buffer)`
Creates a stream with an existing buffer:
```java
public Stream(byte[] buffer) {
    this.buffer = buffer;
    this.currentOffset = 0;
}
```

## Core Writing Methods

### Basic Data Types

#### `writeByte(int value)`
Writes a single byte:
```java
public void writeByte(int value) {
    ensureCapacity(1);
    buffer[currentOffset++] = (byte) value;
}
```

#### `writeWord(int value)` / `writeWordBigEndian(int value)`
Writes 16-bit integers in different byte orders:
```java
public void writeWord(int value) {
    ensureCapacity(2);
    buffer[currentOffset++] = (byte) (value >> 8);  // High byte first
    buffer[currentOffset++] = (byte) value;         // Low byte second
}

public void writeWordBigEndian(int value) {
    ensureCapacity(2);
    buffer[currentOffset++] = (byte) value;         // Low byte first
    buffer[currentOffset++] = (byte) (value >> 8);  // High byte second
}
```

#### `writeDWord(int value)`
Writes 32-bit integers:
```java
public void writeDWord(int value) {
    ensureCapacity(4);
    buffer[currentOffset++] = (byte) (value >> 24);
    buffer[currentOffset++] = (byte) (value >> 16);
    buffer[currentOffset++] = (byte) (value >> 8);
    buffer[currentOffset++] = (byte) value;
}
```

#### `writeQWord(long value)`
Writes 64-bit long values:
```java
public void writeQWord(long value) {
    ensureCapacity(8);
    buffer[currentOffset++] = (byte) (int) (value >> 56);
    buffer[currentOffset++] = (byte) (int) (value >> 48);
    buffer[currentOffset++] = (byte) (int) (value >> 40);
    buffer[currentOffset++] = (byte) (int) (value >> 32);
    buffer[currentOffset++] = (byte) (int) (value >> 24);
    buffer[currentOffset++] = (byte) (int) (value >> 16);
    buffer[currentOffset++] = (byte) (int) (value >> 8);
    buffer[currentOffset++] = (byte) (int) value;
}
```

### Specialized Byte Operations

#### Transformed Byte Writing
The RuneScape protocol uses various byte transformations for obfuscation:

```java
public void writeByteA(int value) {
    ensureCapacity(1);
    buffer[currentOffset++] = (byte) (value + 128);  // Add 128
}

public void writeByteS(int value) {
    ensureCapacity(1);
    buffer[currentOffset++] = (byte) (128 - value);  // Subtract from 128
}

public void writeByteC(int value) {
    ensureCapacity(1);
    buffer[currentOffset++] = (byte) -value;         // Negate
}
```

#### Specialized Word Operations
```java
public void writeWordBigEndianA(int value) {
    ensureCapacity(2);
    buffer[currentOffset++] = (byte) (value + 128);  // Transform first byte
    buffer[currentOffset++] = (byte) (value >> 8);
}

public void writeWordA(int value) {
    ensureCapacity(2);
    buffer[currentOffset++] = (byte) (value >> 8);
    buffer[currentOffset++] = (byte) (value + 128);  // Transform second byte
}
```

### String and Array Operations

#### `writeString(String s)`
Writes a null-terminated string:
```java
public void writeString(String s) {
    ensureCapacity(s.length() + 1);
    System.arraycopy(s.getBytes(), 0, buffer, currentOffset, s.length());
    currentOffset += s.length();
    buffer[currentOffset++] = 10;  // Newline terminator
}
```

#### `writeBytes(byte[] data, int length, int offset)`
Writes raw byte arrays:
```java
public void writeBytes(byte[] data, int length, int offset) {
    ensureCapacity(length);
    for (int i = offset; i < offset + length; i++) {
        buffer[currentOffset++] = data[i];
    }
}
```

## Packet Construction

### Fixed-Size Packets

#### `createFrame(int packetId)`
Creates a packet with a fixed size:
```java
public void createFrame(int packetId) {
    ensureCapacity(1);
    buffer[currentOffset++] = (byte) (packetId + packetEncryption.nextInt());
}
```

### Variable-Size Packets

#### `createFrameVarSize(int packetId)`
Creates a packet with 8-bit size header:
```java
public void createFrameVarSize(int packetId) {
    ensureCapacity(3);
    buffer[currentOffset++] = (byte) (packetId + packetEncryption.nextInt());
    buffer[currentOffset++] = 0;  // Size placeholder
    
    if (frameStackPtr >= frameStackSize - 1) {
        throw new RuntimeException("Stack overflow");
    }
    frameStack[++frameStackPtr] = currentOffset;
}
```

#### `createFrameVarSizeWord(int packetId)`
Creates a packet with 16-bit size header:
```java
public void createFrameVarSizeWord(int packetId) {
    ensureCapacity(3);
    buffer[currentOffset++] = (byte) (packetId + packetEncryption.nextInt());
    writeWord(0);  // 16-bit size placeholder
    
    if (frameStackPtr >= frameStackSize - 1) {
        throw new RuntimeException("Stack overflow");
    }
    frameStack[++frameStackPtr] = currentOffset;
}
```

#### Ending Variable-Size Packets
```java
public void endFrameVarSize() {
    if (frameStackPtr < 0) {
        throw new RuntimeException("Stack empty");
    }
    writeFrameSize(currentOffset - frameStack[frameStackPtr--]);
}

public void endFrameVarSizeWord() {
    if (frameStackPtr < 0) {
        throw new RuntimeException("Stack empty");
    }
    writeFrameSizeWord(currentOffset - frameStack[frameStackPtr--]);
}
```

## Bit-Level Operations

### Bit Access Mode
```java
public void initBitAccess() {
    bitPosition = currentOffset * 8;  // Convert byte position to bit position
}

public void finishBitAccess() {
    currentOffset = (bitPosition + 7) / 8;  // Convert back to byte position
}
```

### Writing Bits
```java
public void writeBits(int numBits, int value) {
    ensureCapacity((int) Math.ceil(numBits * 8) * 4);
    
    int bytePos = bitPosition >> 3;
    int bitOffset = 8 - (bitPosition & 7);
    bitPosition += numBits;
    
    // Complex bit manipulation to pack values efficiently
    for (; numBits > bitOffset; bitOffset = 8) {
        buffer[bytePos] &= ~bitMaskOut[bitOffset];
        buffer[bytePos++] |= value >> numBits - bitOffset & bitMaskOut[bitOffset];
        numBits -= bitOffset;
    }
    
    if (numBits == bitOffset) {
        buffer[bytePos] &= ~bitMaskOut[bitOffset];
        buffer[bytePos] |= value & bitMaskOut[bitOffset];
    } else {
        buffer[bytePos] &= ~(bitMaskOut[numBits] << bitOffset - numBits);
        buffer[bytePos] |= (value & bitMaskOut[numBits]) << bitOffset - numBits;
    }
}
```

### Bit Masks
```java
public static int bitMaskOut[] = new int[32];
static {
    for (int i = 0; i < 32; i++) {
        bitMaskOut[i] = (1 << i) - 1;  // Creates masks: 0, 1, 3, 7, 15, 31, etc.
    }
}
```

## Memory Management

### Dynamic Buffer Resizing
```java
public void ensureCapacity(int length) {
    if (currentOffset + length + 1 >= buffer.length) {
        byte[] oldBuffer = buffer;
        int newLength = buffer.length * 2;  // Double the size
        buffer = new byte[newLength];
        System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
        ensureCapacity(length);  // Recursive call to ensure sufficient space
    }
}
```

### Buffer Reset
```java
public void reset() {
    if (!(currentOffset > Constants.BUFFER_SIZE)) {
        byte[] oldBuffer = buffer;
        buffer = new byte[Constants.BUFFER_SIZE];
        for (int i = 0; i < currentOffset; i++) {
            buffer[i] = oldBuffer[i];
        }
    }
}
```

## Usage Examples

### Basic Packet Creation
```java
// Create a simple fixed-size packet
Stream stream = new Stream(new byte[1024]);
stream.createFrame(123);  // Packet ID 123
stream.writeByte(42);     // Some data
stream.writeWord(1000);   // More data
```

### Variable-Size Packet
```java
// Create a variable-size packet
stream.createFrameVarSizeWord(53);  // Item update packet
stream.writeWord(3214);             // Interface ID
stream.writeWord(28);               // Number of items

for (int i = 0; i < 28; i++) {
    if (items[i] > 254) {
        stream.writeByte(255);
        stream.writeDWord_v2(items[i]);
    } else {
        stream.writeByte(items[i]);
    }
    stream.writeWordBigEndianA(itemIds[i]);
}

stream.endFrameVarSizeWord();  // Finalize packet size
```

### Bit Packing (Player Updates)
```java
// Player movement updates use bit packing for efficiency
stream.initBitAccess();
stream.writeBits(8, playerCount);  // Number of players

for (Player player : players) {
    if (player.didTeleport) {
        stream.writeBits(1, 1);  // Update required
        stream.writeBits(2, 3);  // Teleport flag
        stream.writeBits(7, player.localX);
        stream.writeBits(7, player.localY);
        stream.writeBits(1, 1);  // Has update block
    } else if (player.moved) {
        stream.writeBits(1, 1);  // Update required
        stream.writeBits(2, 1);  // Movement flag
        stream.writeBits(3, player.direction);
        stream.writeBits(1, 0);  // No update block
    } else {
        stream.writeBits(1, 0);  // No update
    }
}

stream.finishBitAccess();
```

### String and Text Data
```java
// Writing chat messages
stream.createFrameVarSize(253);
stream.writeString("Hello, world!");
stream.endFrameVarSize();

// Writing interface text
stream.createFrameVarSizeWord(126);
stream.writeString("Welcome to 2006Scape!");
stream.writeWordA(interfaceId);
stream.endFrameVarSizeWord();
```

### Complex Data Structures
```java
// Writing item container data
stream.createFrameVarSizeWord(53);
stream.writeWord(containerId);
stream.writeWord(itemCount);

for (int i = 0; i < itemCount; i++) {
    int amount = itemAmounts[i];
    int id = itemIds[i];
    
    // Handle large amounts
    if (amount > 254) {
        stream.writeByte(255);
        stream.writeDWord_v2(amount);
    } else {
        stream.writeByte(amount);
    }
    
    // Write item ID with transformation
    stream.writeWordBigEndianA(id > 0 ? id + 1 : 0);
}

stream.endFrameVarSizeWord();
```

## Protocol-Specific Features

### ISAAC Encryption Integration
```java
// Packet IDs are encrypted using ISAAC cipher
public void createFrame(int packetId) {
    ensureCapacity(1);
    buffer[currentOffset++] = (byte) (packetId + packetEncryption.nextInt());
}
```

### RuneScape Data Transformations
The protocol uses various transformations to obfuscate data:
- **Type A**: Add 128 to value
- **Type S**: Subtract value from 128  
- **Type C**: Negate value
- **Big Endian**: Reverse byte order

### Specialized Read Methods
```java
public int readDWord_v1() {
    currentOffset += 4;
    return ((buffer[currentOffset - 2] & 0xff) << 24) +
           ((buffer[currentOffset - 1] & 0xff) << 16) +
           ((buffer[currentOffset - 4] & 0xff) << 8) +
           (buffer[currentOffset - 3] & 0xff);
}

public int readDWord_v2() {
    currentOffset += 4;
    return ((buffer[currentOffset - 3] & 0xff) << 24) +
           ((buffer[currentOffset - 4] & 0xff) << 16) +
           ((buffer[currentOffset - 1] & 0xff) << 8) +
           (buffer[currentOffset - 2] & 0xff);
}
```

## Performance Considerations

### Optimization Strategies
- **Buffer Pre-allocation**: Use appropriate initial buffer sizes
- **Batch Operations**: Group related writes together
- **Bit Packing**: Use bit operations for small values
- **Memory Reuse**: Reset and reuse streams when possible

### Memory Management
- **Dynamic Resizing**: Buffers grow automatically as needed
- **Capacity Planning**: Pre-allocate for known data sizes
- **Cleanup**: Reset streams after use to prevent memory leaks

## Best Practices

1. **Always call ensureCapacity()** before writing data
2. **Use appropriate data types** for the size of your values
3. **Handle variable-size packets correctly** with proper stack management
4. **Initialize bit access** before bit operations
5. **Finish bit access** before returning to byte operations
6. **Reset streams** after use for memory efficiency
7. **Use encryption** for sensitive packet data

## Integration Points

### PacketSender Integration
```java
// PacketSender uses Stream for all packet construction
Player player = getPlayer();
Stream outStream = player.getOutStream();
outStream.createFrame(123);
outStream.writeByte(data);
player.flushOutStream();
```

### Network Layer Integration
```java
// Stream data is sent over network connections
byte[] packetData = new byte[stream.currentOffset];
System.arraycopy(stream.buffer, 0, packetData, 0, stream.currentOffset);
session.write(packetData);
```

### Protocol Handlers
```java
// Incoming packets are read using Stream methods
Stream inStream = new Stream(packetData);
int value1 = inStream.readByte();
int value2 = inStream.readWord();
String text = inStream.readString();
```

## Related Classes

- [`PacketSender`](PacketSender.md) - Uses Stream for packet construction
- [`Player`](Player.md) - Contains output Stream for packet sending
- [`IsaacRandom`](IsaacRandom.md) - Provides packet encryption
- [`Constants`](Constants.md) - Defines buffer sizes and limits
- Network protocol handlers - Use Stream for data serialization
