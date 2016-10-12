package Client.messages;

import java.nio.ByteBuffer;

/**
 * Created by Matteo on 11/10/2016.
 */
public class SerializedMessage {

    private ByteBuffer buffer;
    private String command;
    private int checksum;
    private int size;


    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ByteBuffer getBuffer() {
        return buffer;
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
}
