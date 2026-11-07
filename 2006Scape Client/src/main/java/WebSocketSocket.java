import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * A {@link Socket} that carries the client's raw byte stream inside WebSocket
 * binary frames (RFC 6455).
 *
 * The game, on-demand and JAGGRAB code only ever use getInputStream(),
 * getOutputStream(), setSoTimeout(), setTcpNoDelay() and close(), so this can
 * be handed to them in place of a plain TCP socket. On the server side a
 * WebSocket-to-TCP bridge (e.g. websockify) unwraps the frames and forwards
 * the bytes to the normal game ports.
 *
 * Incoming frames are decoded by a background thread into a buffer, so
 * {@code available()} reports exactly how many payload bytes can be read
 * without blocking - the packet loop in Game relies on that.
 */
final class WebSocketSocket extends Socket {

	private static final String ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private static final int CONNECT_TIMEOUT_MS = 10000;
	private static final int MAX_HANDSHAKE_BYTES = 16384;
	private static final long MAX_FRAME_BYTES = 16L * 1024 * 1024;

	private static final int OP_CONTINUATION = 0x0;
	private static final int OP_TEXT = 0x1;
	private static final int OP_BINARY = 0x2;
	private static final int OP_CLOSE = 0x8;
	private static final int OP_PING = 0x9;
	private static final int OP_PONG = 0xA;

	private static final SecureRandom RANDOM = new SecureRandom();

	private final Socket raw;
	private final InputStream rawIn;
	private final OutputStream rawOut;
	private final Object writeLock = new Object();
	private final ReceiveBuffer receiveBuffer = new ReceiveBuffer();
	private final InputStream payloadIn = new PayloadInputStream();
	private final OutputStream payloadOut = new PayloadOutputStream();
	private volatile int readTimeoutMs;
	private volatile boolean closed;

	/**
	 * Builds the WebSocket URL for one game port: {@code base + "/" + port}.
	 * e.g. wss://game.example.com + 43594 -> wss://game.example.com/43594
	 */
	static String urlForPort(String baseUrl, int port) {
		String base = baseUrl.trim();
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/" + port;
	}

	/** Opens a WebSocket connection to {@code url} (ws:// or wss://). */
	static WebSocketSocket connect(String url) throws IOException {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new IOException("Bad WebSocket URL: " + url, e);
		}
		String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
		boolean secure;
		if (scheme.equals("wss")) {
			secure = true;
		} else if (scheme.equals("ws")) {
			secure = false;
		} else {
			throw new IOException("WebSocket URL must start with ws:// or wss://: " + url);
		}
		String host = uri.getHost();
		if (host == null || host.isEmpty()) {
			throw new IOException("WebSocket URL has no host: " + url);
		}
		int port = uri.getPort() != -1 ? uri.getPort() : (secure ? 443 : 80);
		String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
		if (uri.getRawQuery() != null) {
			path += "?" + uri.getRawQuery();
		}

		Socket raw = new Socket();
		try {
			raw.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
			raw.setTcpNoDelay(true);
			if (secure) {
				SSLSocket ssl = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
						.createSocket(raw, host, port, true);
				SSLParameters params = ssl.getSSLParameters();
				params.setEndpointIdentificationAlgorithm("HTTPS");
				ssl.setSSLParameters(params);
				ssl.startHandshake();
				raw = ssl;
			}
			boolean defaultPort = port == (secure ? 443 : 80);
			String hostHeader = (host.indexOf(':') >= 0 ? "[" + host + "]" : host) + (defaultPort ? "" : ":" + port);
			raw.setSoTimeout(CONNECT_TIMEOUT_MS);
			handshake(raw, hostHeader, path);
			raw.setSoTimeout(0);
			return new WebSocketSocket(raw);
		} catch (IOException e) {
			try {
				raw.close();
			} catch (IOException ignored) {
			}
			throw e;
		}
	}

	private static void handshake(Socket raw, String hostHeader, String path) throws IOException {
		byte[] keyBytes = new byte[16];
		RANDOM.nextBytes(keyBytes);
		String key = Base64.getEncoder().encodeToString(keyBytes);
		String request = "GET " + path + " HTTP/1.1\r\n"
				+ "Host: " + hostHeader + "\r\n"
				+ "Upgrade: websocket\r\n"
				+ "Connection: Upgrade\r\n"
				+ "Sec-WebSocket-Key: " + key + "\r\n"
				+ "Sec-WebSocket-Version: 13\r\n"
				+ "Sec-WebSocket-Protocol: binary\r\n"
				+ "User-Agent: 2006Scape-Client\r\n"
				+ "\r\n";
		OutputStream out = raw.getOutputStream();
		out.write(request.getBytes(StandardCharsets.US_ASCII));
		out.flush();

		String response = readHttpHeaders(raw.getInputStream());
		String[] lines = response.split("\r\n");
		String status = lines.length > 0 ? lines[0] : "";
		String[] statusParts = status.split(" ");
		if (statusParts.length < 2 || !statusParts[1].equals("101")) {
			throw new IOException("WebSocket handshake rejected: " + status);
		}
		String accept = null;
		for (int i = 1; i < lines.length; i++) {
			int colon = lines[i].indexOf(':');
			if (colon > 0 && lines[i].substring(0, colon).trim().equalsIgnoreCase("Sec-WebSocket-Accept")) {
				accept = lines[i].substring(colon + 1).trim();
			}
		}
		if (accept == null || !accept.equals(expectedAccept(key))) {
			throw new IOException("WebSocket handshake failed: bad Sec-WebSocket-Accept");
		}
	}

	/** Reads bytes up to and including the blank line that ends the HTTP headers. */
	private static String readHttpHeaders(InputStream in) throws IOException {
		ByteArrayOutputStream headers = new ByteArrayOutputStream();
		int matched = 0; // progress through "\r\n\r\n"
		while (matched < 4) {
			int b = in.read();
			if (b == -1) {
				throw new EOFException("Connection closed during WebSocket handshake");
			}
			headers.write(b);
			if (headers.size() > MAX_HANDSHAKE_BYTES) {
				throw new IOException("WebSocket handshake response too large");
			}
			if ((matched == 0 || matched == 2) && b == '\r') {
				matched++;
			} else if ((matched == 1 || matched == 3) && b == '\n') {
				matched++;
			} else {
				matched = b == '\r' ? 1 : 0;
			}
		}
		return new String(headers.toByteArray(), StandardCharsets.ISO_8859_1);
	}

	static String expectedAccept(String key) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] digest = sha1.digest((key + ACCEPT_GUID).getBytes(StandardCharsets.US_ASCII));
			return Base64.getEncoder().encodeToString(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private WebSocketSocket(Socket raw) throws IOException {
		this.raw = raw;
		this.rawIn = raw.getInputStream();
		this.rawOut = raw.getOutputStream();
		Thread reader = new Thread(new Runnable() {
			@Override
			public void run() {
				readLoop();
			}
		}, "WebSocket reader");
		reader.setDaemon(true);
		reader.start();
	}

	// ---- Socket overrides used by the client ----

	@Override
	public InputStream getInputStream() throws IOException {
		if (closed) {
			throw new SocketException("Socket is closed");
		}
		return payloadIn;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (closed) {
			throw new SocketException("Socket is closed");
		}
		return payloadOut;
	}

	@Override
	public void setSoTimeout(int timeout) throws SocketException {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout can't be negative");
		}
		readTimeoutMs = timeout;
	}

	@Override
	public int getSoTimeout() {
		return readTimeoutMs;
	}

	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
		raw.setTcpNoDelay(on);
	}

	@Override
	public boolean getTcpNoDelay() throws SocketException {
		return raw.getTcpNoDelay();
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() throws IOException {
		if (closed) {
			return;
		}
		closed = true;
		try {
			sendFrame(OP_CLOSE, new byte[] { 0x03, (byte) 0xE8 }, 0, 2); // 1000 normal closure
		} catch (IOException ignored) {
		}
		receiveBuffer.finish(null);
		raw.close();
	}

	@Override
	public String toString() {
		return "WebSocketSocket[" + raw + "]";
	}

	// ---- Reading ----

	private void readLoop() {
		IOException failure = null;
		try {
			ByteArrayOutputStream fragments = null;
			while (!closed) {
				int b0 = rawIn.read();
				if (b0 == -1) {
					break;
				}
				int b1 = readByte();
				boolean fin = (b0 & 0x80) != 0;
				int opcode = b0 & 0x0F;
				boolean masked = (b1 & 0x80) != 0;
				long length = b1 & 0x7F;
				if (length == 126) {
					length = (readByte() << 8) | readByte();
				} else if (length == 127) {
					length = 0;
					for (int i = 0; i < 8; i++) {
						length = (length << 8) | readByte();
					}
				}
				if (length < 0 || length > MAX_FRAME_BYTES) {
					throw new IOException("WebSocket frame too large: " + length);
				}
				byte[] mask = null;
				if (masked) {
					mask = new byte[4];
					readFully(mask, 4);
				}
				byte[] payload = new byte[(int) length];
				readFully(payload, payload.length);
				if (mask != null) {
					for (int i = 0; i < payload.length; i++) {
						payload[i] ^= mask[i & 3];
					}
				}

				switch (opcode) {
				case OP_BINARY:
				case OP_TEXT:
					if (fin) {
						receiveBuffer.write(payload);
					} else {
						fragments = new ByteArrayOutputStream();
						fragments.write(payload);
					}
					break;
				case OP_CONTINUATION:
					if (fragments == null) {
						throw new IOException("Unexpected WebSocket continuation frame");
					}
					fragments.write(payload);
					if (fin) {
						receiveBuffer.write(fragments.toByteArray());
						fragments = null;
					}
					break;
				case OP_PING:
					sendFrame(OP_PONG, payload, 0, payload.length);
					break;
				case OP_PONG:
					break;
				case OP_CLOSE:
					if (!closed) {
						try {
							sendFrame(OP_CLOSE, payload, 0, Math.min(payload.length, 2));
						} catch (IOException ignored) {
						}
					}
					return;
				default:
					throw new IOException("Unknown WebSocket opcode: " + opcode);
				}
			}
		} catch (IOException e) {
			if (!closed) {
				failure = e;
			}
		} finally {
			receiveBuffer.finish(failure);
		}
	}

	private int readByte() throws IOException {
		int b = rawIn.read();
		if (b == -1) {
			throw new EOFException("WebSocket closed mid-frame");
		}
		return b;
	}

	private void readFully(byte[] dest, int length) throws IOException {
		int offset = 0;
		while (offset < length) {
			int n = rawIn.read(dest, offset, length - offset);
			if (n == -1) {
				throw new EOFException("WebSocket closed mid-frame");
			}
			offset += n;
		}
	}

	// ---- Writing ----

	private void sendFrame(int opcode, byte[] data, int offset, int length) throws IOException {
		byte[] mask = new byte[4];
		RANDOM.nextBytes(mask);
		int headerLength = 2 + (length > 65535 ? 8 : length > 125 ? 2 : 0) + 4;
		byte[] frame = new byte[headerLength + length];
		int pos = 0;
		frame[pos++] = (byte) (0x80 | opcode);
		if (length <= 125) {
			frame[pos++] = (byte) (0x80 | length);
		} else if (length <= 65535) {
			frame[pos++] = (byte) (0x80 | 126);
			frame[pos++] = (byte) (length >> 8);
			frame[pos++] = (byte) length;
		} else {
			frame[pos++] = (byte) (0x80 | 127);
			long len = length;
			for (int shift = 56; shift >= 0; shift -= 8) {
				frame[pos++] = (byte) (len >> shift);
			}
		}
		System.arraycopy(mask, 0, frame, pos, 4);
		pos += 4;
		for (int i = 0; i < length; i++) {
			frame[pos + i] = (byte) (data[offset + i] ^ mask[i & 3]);
		}
		synchronized (writeLock) {
			rawOut.write(frame);
			rawOut.flush();
		}
	}

	private final class PayloadOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b }, 0, 1);
		}

		@Override
		public void write(byte[] data, int offset, int length) throws IOException {
			if (closed) {
				throw new SocketException("Socket is closed");
			}
			if (length == 0) {
				return;
			}
			sendFrame(OP_BINARY, data, offset, length);
		}

		@Override
		public void close() throws IOException {
			WebSocketSocket.this.close();
		}
	}

	private final class PayloadInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			byte[] one = new byte[1];
			int n = read(one, 0, 1);
			return n == -1 ? -1 : one[0] & 0xFF;
		}

		@Override
		public int read(byte[] dest, int offset, int length) throws IOException {
			return receiveBuffer.read(dest, offset, length, readTimeoutMs);
		}

		@Override
		public int available() {
			return receiveBuffer.available();
		}

		@Override
		public void close() throws IOException {
			WebSocketSocket.this.close();
		}
	}

	/** Growable FIFO of received payload bytes, filled by the reader thread. */
	private static final class ReceiveBuffer {
		private byte[] data = new byte[8192];
		private int start;
		private int size;
		private boolean finished;
		private IOException failure;

		synchronized void write(byte[] bytes) {
			if (finished) {
				return;
			}
			if (size + bytes.length > data.length - start) {
				if (size + bytes.length > data.length) {
					byte[] bigger = new byte[Math.max(data.length * 2, size + bytes.length)];
					System.arraycopy(data, start, bigger, 0, size);
					data = bigger;
				} else {
					System.arraycopy(data, start, data, 0, size);
				}
				start = 0;
			}
			System.arraycopy(bytes, 0, data, start + size, bytes.length);
			size += bytes.length;
			notifyAll();
		}

		synchronized void finish(IOException error) {
			if (!finished) {
				finished = true;
				failure = error;
			}
			notifyAll();
		}

		synchronized int available() {
			return size;
		}

		synchronized int read(byte[] dest, int offset, int length, int timeoutMs) throws IOException {
			if (length == 0) {
				return 0;
			}
			long deadline = timeoutMs > 0 ? System.currentTimeMillis() + timeoutMs : 0;
			while (size == 0) {
				if (finished) {
					if (failure != null) {
						throw new IOException(failure.getMessage(), failure);
					}
					return -1;
				}
				try {
					if (timeoutMs > 0) {
						long remaining = deadline - System.currentTimeMillis();
						if (remaining <= 0) {
							throw new SocketTimeoutException("Read timed out");
						}
						wait(remaining);
					} else {
						wait();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("Interrupted while reading", e);
				}
			}
			int n = Math.min(length, size);
			System.arraycopy(data, start, dest, offset, n);
			start += n;
			size -= n;
			if (size == 0) {
				start = 0;
			}
			return n;
		}
	}
}
