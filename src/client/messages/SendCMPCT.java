package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 */
public class SendCMPCT extends Message {

    private boolean flag;
    private long version;

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String getCommand() {
        return "sendcmpct";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        flag = leis.readBoolean();
        version = leis.readLong();
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeBoolean(flag);
        leos.writeLong(version);
    }

}
