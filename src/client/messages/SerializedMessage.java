package client.messages;

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
    private long id;

    public SerializedMessage(){
        id = -1;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId() {
        return id;
    }

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
