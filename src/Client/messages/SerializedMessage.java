package Client.messages;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class SerializedMessage {

    public static final ConcurrentHashMap<Integer,ConcurrentLinkedQueue<ByteBuffer>> unusedBuffer = new ConcurrentHashMap<>();
    private ByteBuffer header;
    private ByteBuffer payload;
    private String command;
    private int checksum;
    private int size;

    public static ByteBuffer getBuffer(int length){
        ConcurrentLinkedQueue<ByteBuffer> buffers = unusedBuffer.get(length);
        ByteBuffer buffer = null;
        if(buffers != null)
            buffer = buffers.poll();
        if(buffer == null)
            buffer = ByteBuffer.allocate(length);
        buffer.clear();
        return buffer;
    }

    public static void addBuffer(ByteBuffer buffer){
        if(!unusedBuffer.containsKey(buffer.capacity()))
            unusedBuffer.put(buffer.capacity(),new ConcurrentLinkedQueue<>());
        unusedBuffer.get(buffer.capacity()).add(buffer);
    }

    public SerializedMessage(){}

    public void setCommand(String command) {
        this.command = command;
    }

    public void setSize(int size) {
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

    public int getSize() {
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
        return "command: "+command+" size "+size+" checksum "+checksum;
    }
}
