// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

final class RSSocket implements Runnable {

	public RSSocket(RSApplet applet, Socket connection) throws IOException {
		closed = false;
		isWriter = false;
		hasIOError = false;
		rsApplet = applet;
		socket = connection;
		socket.setSoTimeout(30000);
		socket.setTcpNoDelay(true);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}

	public void close() {
		closed = true;
		try {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException closeFailure) {
			System.out.println("Error closing stream");
		}
		isWriter = false;
		synchronized (this) {
			notify();
		}
		buffer = null;
	}

	public int read() throws IOException {
		if (closed) {
			return 0;
		} else {
			return inputStream.read();
		}
	}

	public int available() throws IOException {
		if (closed) {
			return 0;
		} else {
			return inputStream.available();
		}
	}

	public void readFully(byte destination[], int remainingBytes) throws IOException {
		int destinationOffset = 0;// was parameter
		if (closed) {
			return;
		}
		int bytesRead;
		for (; remainingBytes > 0; remainingBytes -= bytesRead) {
			bytesRead = inputStream.read(destination, destinationOffset, remainingBytes);
			if (bytesRead <= 0) {
				throw new IOException("EOF");
			}
			destinationOffset += bytesRead;
		}

	}

	public void queueBytes(int byteCount, byte source[]) throws IOException {
		if (closed) {
			return;
		}
		if (hasIOError) {
			hasIOError = false;
			throw new IOException("Error in writer thread");
		}
		if (buffer == null) {
			buffer = new byte[5000];
		}
		synchronized (this) {
			for (int sourceIndex = 0; sourceIndex < byteCount; sourceIndex++) {
				buffer[enqueueIndex] = source[sourceIndex];
				enqueueIndex = (enqueueIndex + 1) % 5000;
				if (enqueueIndex == (writeIndex + 4900) % 5000) {
					throw new IOException("buffer overflow");
				}
			}

			if (!isWriter) {
				isWriter = true;
				rsApplet.startRunnable(this, 3);
			}
			notify();
		}
	}

	@Override
	public void run() {
		while (isWriter) {
			int contiguousByteCount;
			int bufferOffset;
			synchronized (this) {
				if (enqueueIndex == writeIndex) {
					try {
						wait();
					} catch (InterruptedException waitInterrupted) {
					}
				}
				if (!isWriter) {
					return;
				}
				bufferOffset = writeIndex;
				if (enqueueIndex >= writeIndex) {
					contiguousByteCount = enqueueIndex - writeIndex;
				} else {
					contiguousByteCount = 5000 - writeIndex;
				}
			}
			if (contiguousByteCount > 0) {
				try {
					outputStream.write(buffer, bufferOffset, contiguousByteCount);
				} catch (IOException writeFailure) {
					hasIOError = true;
				}
				writeIndex = (writeIndex + contiguousByteCount) % 5000;
				try {
					if (enqueueIndex == writeIndex) {
						outputStream.flush();
					}
				} catch (IOException flushFailure) {
					hasIOError = true;
				}
			}
		}
	}

	public void printDebug() {
		System.out.println("dummy:" + closed);
		System.out.println("tcycl:" + writeIndex);
		System.out.println("tnum:" + enqueueIndex);
		System.out.println("writer:" + isWriter);
		System.out.println("ioerror:" + hasIOError);
		try {
			System.out.println("available:" + available());
		} catch (IOException debugFailure) {
		}
	}

	private InputStream inputStream;
	private OutputStream outputStream;
	private final Socket socket;
	private boolean closed;
	private final RSApplet rsApplet;
	private byte[] buffer;
	private int writeIndex;
	private int enqueueIndex;
	private boolean isWriter;
	private boolean hasIOError;
}
