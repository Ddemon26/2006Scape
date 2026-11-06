import java.io.*;
import java.net.Socket;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class RSSocketTest {
    static Object get(RSSocket s, String n) throws Exception {
        Field f = RSSocket.class.getDeclaredField(n); f.setAccessible(true); return f.get(s);
    }
    static void set(RSSocket s, String n, Object v) throws Exception {
        Field f = RSSocket.class.getDeclaredField(n); f.setAccessible(true); f.set(s, v);
    }
    static final class App extends RSApplet {
        Runnable task; int starts, priority;
        @Override public void startRunnable(Runnable r, int p) { task = r; starts++; priority = p; }
    }
    static final class In extends InputStream {
        byte[] data = {11,22,33,44,55,66}; int pos, calls; boolean zero, shut, failAvailable, failClose;
        List<String> requests = new ArrayList<String>();
        public int read() { return pos == data.length ? -1 : data[pos++] & 255; }
        public int available() throws IOException { if(failAvailable)throw new IOException("available fixture");return data.length-pos; }
        public int read(byte[] b, int off, int len) {
            calls++; requests.add(off+":"+len);
            if(zero) return 0;
            if(pos==data.length) return -1;
            int n=Math.min(2, Math.min(len,data.length-pos));
            System.arraycopy(data,pos,b,off,n);pos+=n;return n;
        }
        public void close()throws IOException{shut=true;if(failClose)throw new IOException("close fixture");}
    }
    static final class Out extends OutputStream {
        RSSocket owner; boolean failWrite, failFlush, shut; int flushes;
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); List<String> ranges=new ArrayList<String>();
        public void write(int b){bytes.write(b);}
        public void write(byte[] b,int off,int len)throws IOException {
            ranges.add(off+":"+len);if(failWrite)throw new IOException("write fixture");bytes.write(b,off,len);
        }
        public void flush()throws IOException {
            flushes++;try{set(owner,"isWriter",false);}catch(Exception e){throw new AssertionError(e);}
            if(failFlush)throw new IOException("flush fixture");
        }
        public void close(){shut=true;}
    }
    static final class Sock extends Socket {
        In in=new In();Out out=new Out();int timeout;boolean delay,shut;
        public void setSoTimeout(int n){timeout=n;}
        public void setTcpNoDelay(boolean b){delay=b;}
        public InputStream getInputStream(){return in;}
        public OutputStream getOutputStream(){return out;}
        public synchronized void close(){shut=true;}
    }
    static final class F {
        App app=new App();Sock sock=new Sock();RSSocket s;
        F()throws Exception{s=new RSSocket(app,sock);sock.out.owner=s;}
    }
    @Test public void constructorAndFragmentedInput()throws Exception {
        F f=new F();assertEquals(30000,f.sock.timeout);assertTrue(f.sock.delay);
        assertEquals(6,f.s.available());byte[] b={-9,-9,-9,-9,-9,-9,-9};
        f.s.readFully(b,5);assertArrayEquals(new byte[]{11,22,33,44,55,-9,-9},b);
        assertEquals(Arrays.asList("0:5","2:3","4:1"),f.sock.in.requests);
        assertEquals(1,f.s.available());assertEquals(66,f.s.read());assertEquals(-1,f.s.read());
    }
    @Test public void inputBoundariesAndFailurePrefix()throws Exception {
        F f=new F();f.s.readFully(null,0);f.s.readFully(null,-3);assertEquals(0,f.sock.in.calls);
        byte[] b=new byte[8];try{f.s.readFully(b,8);fail();}catch(IOException e){assertEquals("EOF",e.getMessage());}
        assertArrayEquals(new byte[]{11,22,33,44,55,66,0,0},b);
        F z=new F();z.sock.in.zero=true;try{z.s.readFully(new byte[1],1);fail();}catch(IOException e){assertEquals("EOF",e.getMessage());}
        assertEquals(1,z.sock.in.calls);
    }
    @Test public void queueCopiesPrefixAndSchedulesOnce()throws Exception {
        F f=new F();byte[] a={9,8,7,6};f.s.queueBytes(3,a);a[0]=99;f.s.queueBytes(2,new byte[]{5,4,3});
        assertEquals(1,f.app.starts);assertSame(f.s,f.app.task);assertEquals(3,f.app.priority);
        assertEquals(5,get(f.s,"enqueueIndex"));assertEquals(0,get(f.s,"writeIndex"));
        f.s.run();assertArrayEquals(new byte[]{9,8,7,5,4},f.sock.out.bytes.toByteArray());
        assertEquals(Arrays.asList("0:5"),f.sock.out.ranges);assertEquals(1,f.sock.out.flushes);
        assertEquals(5,get(f.s,"writeIndex"));
    }
    @Test public void wrapWritesContiguousEndThenBeginning()throws Exception {
        F f=new F();set(f.s,"enqueueIndex",4998);set(f.s,"writeIndex",4998);
        f.s.queueBytes(5,new byte[]{1,2,3,4,5,99});assertEquals(3,get(f.s,"enqueueIndex"));
        f.s.run();assertArrayEquals(new byte[]{1,2,3,4,5},f.sock.out.bytes.toByteArray());
        assertEquals(Arrays.asList("4998:2","0:3"),f.sock.out.ranges);
        assertEquals(1,f.sock.out.flushes);assertEquals(3,get(f.s,"writeIndex"));
    }
    @Test public void overflowLeaves4900CopiedBytesWithoutStartingWriter()throws Exception {
        F f=new F();byte[] b=new byte[4901];for(int n=0;n<b.length;n++)b[n]=(byte)n;
        try{f.s.queueBytes(b.length,b);fail();}catch(IOException e){assertEquals("buffer overflow",e.getMessage());}
        assertEquals(4900,get(f.s,"enqueueIndex"));assertEquals(0,f.app.starts);
        assertArrayEquals(Arrays.copyOf(b,4900),Arrays.copyOf((byte[])get(f.s,"buffer"),4900));
        assertEquals(0,((byte[])get(f.s,"buffer"))[4900]);
    }
    @Test public void errorLatchReportsOnceAndWriteFailureStillAdvances()throws Exception {
        for(boolean write:new boolean[]{false,true}){
            F f=new F();f.sock.out.failWrite=write;f.sock.out.failFlush=!write;
            f.s.queueBytes(3,new byte[]{4,5,6});f.s.run();assertEquals(3,get(f.s,"writeIndex"));
            assertEquals(true,get(f.s,"hasIOError"));assertEquals(1,f.sock.out.flushes);
            try{f.s.queueBytes(1,new byte[]{7});fail();}catch(IOException e){assertEquals("Error in writer thread",e.getMessage());}
            assertEquals(false,get(f.s,"hasIOError"));assertEquals(3,get(f.s,"enqueueIndex"));
            f.s.queueBytes(1,new byte[]{8});assertEquals(4,get(f.s,"enqueueIndex"));assertEquals(2,f.app.starts);
        }
    }
    @Test public void closedMethodsAreInertAndCloseReleasesResources()throws Exception {
        F f=new F();f.s.queueBytes(1,new byte[]{3});f.s.close();
        assertTrue(f.sock.shut&&f.sock.in.shut&&f.sock.out.shut);assertNull(get(f.s,"buffer"));
        assertEquals(false,get(f.s,"isWriter"));assertEquals(0,f.s.read());assertEquals(0,f.s.available());
        f.s.readFully(null,5);f.s.queueBytes(8,null);f.s.run();assertEquals(0,f.sock.out.ranges.size());
    }
    @Test public void idleWriterWakesOnCloseAndOnInterrupt()throws Exception {
        final F f=new F();f.s.queueBytes(0,new byte[0]);
        Thread t=new Thread(f.s);t.start();
        long end=System.currentTimeMillis()+3000;
        while(t.getState()!=Thread.State.WAITING&&System.currentTimeMillis()<end)Thread.yield();
        assertEquals(Thread.State.WAITING,t.getState());t.interrupt();
        // The interrupted wait is swallowed; closing supplies the terminating notification.
        f.s.close();t.join(3000);assertFalse(t.isAlive());assertEquals(0,f.sock.out.ranges.size());
    }
    @Test public void closeAndDebugCatchTheirDistinctIOFailures()throws Exception {
        F f=new F();f.sock.in.failAvailable=true;
        PrintStream old=System.out;ByteArrayOutputStream capture=new ByteArrayOutputStream();
        try{
            System.setOut(new PrintStream(capture));f.s.printDebug();
            assertTrue(capture.toString("UTF-8").contains("dummy:false"));
            assertFalse(capture.toString("UTF-8").contains("available:"));
            f.sock.in.failClose=true;f.s.close();
            assertTrue(capture.toString("UTF-8").contains("Error closing stream"));
            assertTrue(f.sock.in.shut);assertFalse(f.sock.out.shut);assertFalse(f.sock.shut);
            assertNull(get(f.s,"buffer"));assertEquals(0,f.s.available());
        }finally{System.setOut(old);}
    }
    @Test public void nonpositiveQueueAndShortSourcePreserveExistingOrder()throws Exception {
        F f=new F();f.s.queueBytes(-2,null);assertEquals(0,get(f.s,"enqueueIndex"));assertEquals(1,f.app.starts);
        f.s.close();
        F z=new F();try{z.s.queueBytes(3,new byte[]{7,8});fail();}catch(ArrayIndexOutOfBoundsException expected){}
        assertEquals(2,get(z.s,"enqueueIndex"));assertEquals(0,z.app.starts);
        assertEquals(7,((byte[])get(z.s,"buffer"))[0]);assertEquals(8,((byte[])get(z.s,"buffer"))[1]);
    }
}
