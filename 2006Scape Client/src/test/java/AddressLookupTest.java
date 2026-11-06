import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import org.junit.Test;

public class AddressLookupTest {
    @Test public void fixedVectorsDistinguishOctetOrderAndSignedValues() {
        int[] values = {0, 1, 256, 65536, 16777216, 0x01020304, 0x7f000001,
                0x80000000, 0xffffffff, 0xff0080fe, Integer.MAX_VALUE};
        String[] expected = {"0.0.0.0", "0.0.0.1", "0.0.1.0", "0.1.0.0", "1.0.0.0",
                "1.2.3.4", "127.0.0.1", "128.0.0.0", "255.255.255.255",
                "255.0.128.254", "127.255.255.255"};
        for (int n = 0; n < values.length; n++) {
            assertEquals(expected[n], TextClass.formatIpv4Address(values[n]));
        }
    }

    @Test public void eachByteVariesIndependentlyAgainstNetworkLibrary() throws Exception {
        for (int position = 0; position < 4; position++) {
            for (int value = 0; value < 256; value++) {
                byte[] octets = {17, 34, 68, (byte) 136};
                octets[position] = (byte) value;
                int bits = java.nio.ByteBuffer.wrap(octets).getInt();
                assertEquals(InetAddress.getByAddress(octets).getHostAddress(), TextClass.formatIpv4Address(bits));
                assertEquals("1.2.3.4", TextClass.formatIpv4Address(0x01020304));
            }
        }
    }

    @Test public void requestPublishesInputAndReplacesTheSinglePendingSlot() throws Exception {
        Field field = Signlink.class.getDeclaredField("pendingDnsAddress");
        field.setAccessible(true);
        synchronized (Signlink.class) {
            Object previous = field.get(null);
            String result = Signlink.dnsLookupResult;
            try {
                String first = new String("127.0.0.1");
                String second = new String("bad/address");
                Signlink.dnslookup(first);
                assertSame(first, Signlink.dnsLookupResult);
                assertSame(first, field.get(null));
                Signlink.dnslookup(second);
                assertSame(second, Signlink.dnsLookupResult);
                assertSame(second, field.get(null));
                assertNotSame(first, field.get(null));
                Signlink.dnslookup("");
                assertEquals("", field.get(null));
                assertEquals("", Signlink.dnsLookupResult);
            } finally {
                field.set(null, previous);
                Signlink.dnsLookupResult = result;
            }
        }
    }

    @Test public void nullClearsBothSlotsAndRequestIsStaticSynchronized() throws Exception {
        Field field = Signlink.class.getDeclaredField("pendingDnsAddress");
        field.setAccessible(true);
        synchronized (Signlink.class) {
            Object previous = field.get(null);
            String result = Signlink.dnsLookupResult;
            try {
                Signlink.dnslookup("127.0.0.1");
                Signlink.dnslookup(null);
                assertNull(field.get(null));
                assertNull(Signlink.dnsLookupResult);
            } finally {
                field.set(null, previous);
                Signlink.dnsLookupResult = result;
            }
        }
        int flags = Signlink.class.getDeclaredMethod("dnslookup", String.class).getModifiers();
        assertTrue(Modifier.isStatic(flags));
        assertTrue(Modifier.isSynchronized(flags));
    }
}
