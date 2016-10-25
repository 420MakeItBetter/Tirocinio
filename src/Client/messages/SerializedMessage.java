package Client.messages;

import java.nio.ByteBuffer;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class SerializedMessage {

    private ByteBuffer header;
    private ByteBuffer payload;
    private String command;
    private int checksum;
    private int size;


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
