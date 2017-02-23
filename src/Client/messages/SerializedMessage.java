package Client.messages;

import Client.BitConstants;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class SerializedMessage {

    public static final BlockingQueue<ByteBuffer> headers = new LinkedBlockingQueue<>();
    public static final BlockingQueue<ByteBuffer> payloads = new LinkedBlockingQueue<>();
    public static final AtomicLong headerC = new AtomicLong();
    public static final AtomicLong payloadC = new AtomicLong();

    private ByteBuffer header;
    private ByteBuffer payload;
    private String command;
    private int checksum;
    private long size;

    /*
    public static void initializeBuffers(){
        //creo 4.166.666 bytebuffer per gli header
        for(int i = 0; i < BitConstants.MEGA*100; i+=BitConstants.HEADERLENGTH)
        {
            headers.add(ByteBuffer.allocate(BitConstants.HEADERLENGTH));
            headerC.incrementAndGet();
        }

        //creo 6.249.999 bytebuffer di 500 byte per i payload
        for(long i = 0; i < BitConstants.GIGA+(BitConstants.MEGA*125); i+=500)
        {
            payloads.add(ByteBuffer.allocate(500));
            payloadC.incrementAndGet();
        }
    }

    public static ByteBuffer newHeader() {
        ByteBuffer b = headers.poll();
        headerC.decrementAndGet();
        return b;
    }

    public static ByteBuffer [] newPayload(long size) {
        long l = size/500;
        boolean failed = false;
        ByteBuffer [] ret = new ByteBuffer [(int) (l + 1)];
        int i;
        for(i = 0; i < l; i+=1)
        {
            ret[i] = payloads.poll();
            if(ret[i] == null)
            {
                failed = true;
                break;
            }
            payloadC.decrementAndGet();
        }
        if(failed)
            for(int j = 0; j < i; j++)
            {
                payloadC.incrementAndGet();
                payloads.add(ret[j]);
            }
        else
        {
            ret[ret.length - 1] = payloads.poll();
            if(ret[ret.length - 1] == null)
                for(int j = 0; j < ret.length - 1; j++)
                {
                    payloadC.incrementAndGet();
                    payloads.add(ret[j]);
                }
            else
            {
                payloadC.decrementAndGet();
                ret[ret.length - 1].limit((int) (size - l * 500));
                return ret;
            }
        }
        return null;
    }

    public static ByteBuffer newBlockingHeader() throws InterruptedException {
        ByteBuffer b = headers.take();
        headerC.decrementAndGet();
        return b;
    }

    public static ByteBuffer[] newBlockingPayload(int size) throws InterruptedException {
        int l = size/500;
        ByteBuffer [] ret = new ByteBuffer [l + 1];
        int i;
        for(i = 0; i < l; i+=1)
        {
            ret[i] = payloads.take();
            payloadC.decrementAndGet();
        }
        payloadC.decrementAndGet();
        ret[ret.length - 1] = payloads.take();
        ret[ret.length - 1].limit(size - l * 500);
        return ret;
    }

    public static void returnHeader(ByteBuffer header) throws InterruptedException {
        if(header == null)
            return;
        header.clear();
        headerC.incrementAndGet();
        headers.put(header);
    }

    public static void returnPayload(ByteBuffer [] payload) throws InterruptedException {
        if(payload == null)
            return;
        for(int i = 0; i < payload.length; i++)
        {
            payloadC.incrementAndGet();
            payload[i].clear();
            payloads.put(payload[i]);
        }
    }
    */
    public SerializedMessage(){}


    public void setCommand(String command) {
        this.command = command;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setHeader(ByteBuffer header) {
        this.header = header;
    }

    public ByteBuffer getHeader() {
        return header;
    }

    public void setPayload(ByteBuffer payload) {
        this.payload = payload;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public String getCommand() {
        return command;
    }

    public long getSize() {
        return size;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getChecksum() {
        return checksum;
    }

    @Override
    public String toString() {
        return "command: "+command+" size "+size+" checksum "+checksum+" header "+header+" payload: "+payload;
    }

    public void flipPayload() {
        payload.rewind();
    }
}
