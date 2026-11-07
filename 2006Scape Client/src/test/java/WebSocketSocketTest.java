import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Exercises WebSocketSocket against a tiny in-process WebSocket server, so the
 * handshake, framing, ping handling, available() and timeouts are covered
 * without needing websockify or a network.
 */
public class WebSocketSocketTest {

	private ServerSocket server;
	private final AtomicReference<String> requestSeen = new AtomicReference<>();

	@Before
	public void setUp() throws IOException {
		server = new ServerSocket(0);
	}

	@After
	public void tearDown() throws IOException {
		server.close();
		ClientSettings.WEBSOCKET_URL = null;
	}

	interface Script {
		void run(InputStream in, OutputStream out) throws Exception;
	}

	private Thread serve(final Script script) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try (Socket s = server.accept()) {
					InputStream in = s.getInputStream();
					OutputStream out = s.getOutputStream();
					String request = readHeaders(in);
					requestSeen.set(request);
					String key = header(request, "Sec-WebSocket-Key");
					out.write(("HTTP/1.1 101 Switching Protocols\r\n"
							+ "Upgrade: websocket\r\nConnection: Upgrade\r\n"
							+ "Sec-WebSocket-Accept: " + WebSocketSocket.expectedAccept(key) + "\r\n"
							+ "Sec-WebSocket-Protocol: binary\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
					out.flush();
					script.run(in, out);
				} catch (Exception e) {
					// test assertions on the client side report failures
				}
			}
		});
		t.setDaemon(true);
		t.start();
		return t;
	}

	private String url(String path) {
		return "ws://127.0.0.1:" + server.getLocalPort() + path;
	}

	@Test
	public void urlForPortAppendsPort() {
		assertEquals("wss://game.example.com/43594", WebSocketSocket.urlForPort("wss://game.example.com", 43594));
		assertEquals("wss://game.example.com/43595", WebSocketSocket.urlForPort(" wss://game.example.com// ", 43595));
	}

	@Test
	public void acceptKeyMatchesRfcExample() {
		// RFC 6455 section 1.3
		assertEquals("s3pPLMBiTxaQ9kYGzzhZRbK+xOo=", WebSocketSocket.expectedAccept("dGhlIHNhbXBsZSBub25jZQ=="));
	}

	@Test
	public void handshakeSendsPathHostAndBinaryProtocol() throws Exception {
		serve(new Script() {
			@Override
			public void run(InputStream in, OutputStream out) throws Exception {
				readClientFrame(in);
			}
		});
		WebSocketSocket ws = WebSocketSocket.connect(url("/43594"));
		ws.close();
		String req = requestSeen.get();
		assertTrue(req, req.startsWith("GET /43594 HTTP/1.1\r\n"));
		assertEquals("127.0.0.1:" + server.getLocalPort(), header(req, "Host"));
		assertEquals("binary", header(req, "Sec-WebSocket-Protocol"));
		assertEquals("13", header(req, "Sec-WebSocket-Version"));
	}

	@Test
	public void echoesBytesAndReassemblesAcrossFrames() throws Exception {
		serve(new Script() {
			@Override
			public void run(InputStream in, OutputStream out) throws Exception {
				byte[] got = readClientFrame(in)[1];
				// Echo back split across a fragmented message plus a ping in between.
				int half = got.length / 2;
				writeServerFrame(out, 0x02, false, slice(got, 0, half));
				writeServerFrame(out, 0x09, true, new byte[] { 7 });
				writeServerFrame(out, 0x00, true, slice(got, half, got.length - half));
				byte[][] pong = readClientFrame(in);
				// Report the pong opcode back as a one-byte binary frame.
				writeServerFrame(out, 0x02, true, new byte[] { pong[0][0], pong[1][0] });
				readClientFrame(in); // close
			}
		});
		WebSocketSocket ws = WebSocketSocket.connect(url("/43594"));
		ws.setSoTimeout(5000);
		byte[] sent = new byte[300]; // > 125 to exercise the 16-bit length
		for (int i = 0; i < sent.length; i++) {
			sent[i] = (byte) (i * 31);
		}
		ws.getOutputStream().write(sent, 0, sent.length);

		DataInputStream in = new DataInputStream(ws.getInputStream());
		byte[] back = new byte[sent.length];
		in.readFully(back);
		assertArrayEquals(sent, back);

		byte[] pongReport = new byte[2];
		in.readFully(pongReport);
		assertEquals(0x0A, pongReport[0]);
		assertEquals(7, pongReport[1]);
		ws.close();
	}

	@Test
	public void availableReportsBufferedBytesWithoutBlocking() throws Exception {
		serve(new Script() {
			@Override
			public void run(InputStream in, OutputStream out) throws Exception {
				writeServerFrame(out, 0x02, true, new byte[] { 1, 2, 3 });
				writeServerFrame(out, 0x02, true, new byte[] { 4, 5 });
				readClientFrame(in);
			}
		});
		WebSocketSocket ws = WebSocketSocket.connect(url("/43594"));
		InputStream in = ws.getInputStream();
		long deadline = System.currentTimeMillis() + 5000;
		while (in.available() < 5 && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertEquals(5, in.available());
		assertEquals(1, in.read());
		assertEquals(4, in.available());
		ws.close();
	}

	@Test
	public void readTimesOutWhenServerIsSilent() throws Exception {
		serve(new Script() {
			@Override
			public void run(InputStream in, OutputStream out) throws Exception {
				readClientFrame(in);
			}
		});
		WebSocketSocket ws = WebSocketSocket.connect(url("/43595"));
		ws.setSoTimeout(200);
		long start = System.currentTimeMillis();
		try {
			ws.getInputStream().read();
			fail("expected timeout");
		} catch (SocketTimeoutException expected) {
			assertTrue(System.currentTimeMillis() - start >= 150);
		}
		ws.close();
	}

	@Test
	public void serverCloseGivesEndOfStream() throws Exception {
		serve(new Script() {
			@Override
			public void run(InputStream in, OutputStream out) throws Exception {
				writeServerFrame(out, 0x02, true, new byte[] { 9 });
				writeServerFrame(out, 0x08, true, new byte[] { 0x03, (byte) 0xE8 });
				readClientFrame(in);
			}
		});
		WebSocketSocket ws = WebSocketSocket.connect(url("/43594"));
		ws.setSoTimeout(5000);
		InputStream in = ws.getInputStream();
		assertEquals(9, in.read());
		assertEquals(-1, in.read());
		ws.close();
	}

	@Test
	public void rejectedHandshakeThrows() throws Exception {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try (Socket s = server.accept()) {
					readHeaders(s.getInputStream());
					s.getOutputStream().write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n"
							.getBytes(StandardCharsets.US_ASCII));
				} catch (IOException ignored) {
				}
			}
		});
		t.setDaemon(true);
		t.start();
		try {
			WebSocketSocket.connect(url("/43594"));
			fail("expected handshake failure");
		} catch (IOException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("404"));
		}
	}

	@Test
	public void badSchemeIsRejected() {
		try {
			WebSocketSocket.connect("http://example.com/43594");
			fail("expected failure");
		} catch (IOException expected) {
			assertTrue(expected.getMessage().contains("ws://"));
		}
	}

	@Test
	public void wsArgumentSetsUrl() {
		ClientArgs.apply(new String[] { "-ws", "wss://game.example.com" }, null);
		assertEquals("wss://game.example.com", ClientSettings.WEBSOCKET_URL);
	}

	@Test
	public void gameOpenSocketUsesWebSocketWhenConfigured() throws Exception {
		// Game extends Applet, which can't be constructed without a display.
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		serve(new Script() {
			@Override
			public void run(InputStream in, OutputStream out) throws Exception {
				readClientFrame(in);
			}
		});
		ClientSettings.WEBSOCKET_URL = "ws://127.0.0.1:" + server.getLocalPort();
		Game game = new Game();
		Socket socket = game.openSocket(43595);
		assertTrue(socket instanceof WebSocketSocket);
		socket.close();
		assertTrue(requestSeen.get().startsWith("GET /43595 HTTP/1.1\r\n"));
	}

	// ---- helpers ----

	static String readHeaders(InputStream in) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int state = 0;
		while (state < 4) {
			int b = in.read();
			if (b == -1) {
				throw new IOException("eof");
			}
			buf.write(b);
			if ((state % 2 == 0 && b == '\r') || (state % 2 == 1 && b == '\n')) {
				state++;
			} else {
				state = b == '\r' ? 1 : 0;
			}
		}
		return new String(buf.toByteArray(), StandardCharsets.ISO_8859_1);
	}

	static String header(String headers, String name) {
		for (String line : headers.split("\r\n")) {
			int colon = line.indexOf(':');
			if (colon > 0 && line.substring(0, colon).trim().equalsIgnoreCase(name)) {
				return line.substring(colon + 1).trim();
			}
		}
		return null;
	}

	/** Reads one masked client frame; returns {{opcode}, payload}. Asserts it is masked. */
	static byte[][] readClientFrame(InputStream in) throws IOException {
		DataInputStream d = new DataInputStream(in);
		int b0 = d.readUnsignedByte();
		int b1 = d.readUnsignedByte();
		if ((b1 & 0x80) == 0) {
			throw new IOException("client frame not masked");
		}
		long len = b1 & 0x7F;
		if (len == 126) {
			len = d.readUnsignedShort();
		} else if (len == 127) {
			len = d.readLong();
		}
		byte[] mask = new byte[4];
		d.readFully(mask);
		byte[] payload = new byte[(int) len];
		d.readFully(payload);
		for (int i = 0; i < payload.length; i++) {
			payload[i] ^= mask[i & 3];
		}
		return new byte[][] { { (byte) (b0 & 0x0F) }, payload };
	}

	static void writeServerFrame(OutputStream out, int opcode, boolean fin, byte[] payload) throws IOException {
		ByteArrayOutputStream f = new ByteArrayOutputStream();
		f.write((fin ? 0x80 : 0) | opcode);
		if (payload.length <= 125) {
			f.write(payload.length);
		} else {
			f.write(126);
			f.write(payload.length >> 8);
			f.write(payload.length);
		}
		f.write(payload);
		out.write(f.toByteArray());
		out.flush();
	}

	static byte[] slice(byte[] src, int off, int len) {
		byte[] out = new byte[len];
		System.arraycopy(src, off, out, 0, len);
		return out;
	}
}
