package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 */
public class FilterLoad extends Message {

    private byte [] filter;
    private int nHashFuncs;
    private int nTweak;
    private byte nFlags;

    public void setFilter(byte[] filter) {
        this.filter = filter;
    }

    public void setnHashFuncs(int nHashFuncs) {
        this.nHashFuncs = nHashFuncs;
    }

    public void setnTweak(int nTweak) {
        this.nTweak = nTweak;
    }

    public void setnFlags(byte nFlags) {
        this.nFlags = nFlags;
    }

    @Override
    public String getCommand() {
        return "filterload";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        int length = (int) (getLength() - 9);
        filter = new byte [length];
        leis.read(filter);
        nHashFuncs = leis.readInt();
        nTweak = leis.readInt();
        nFlags = (byte) leis.read();
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.write(filter);
        leos.writeInt(nHashFuncs);
        leos.writeInt(nTweak);
        leos.write(nFlags);
    }
}
