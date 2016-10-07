package messages;

import bitio.LittleEndianInputStream;
import bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 07/10/2016.
 */
public class Address extends Message {

    private static final int MAX_ENTRIES = 1000;

    private List<PeerAddress> addresses;

    public Address(){
        addresses = new ArrayList<>();
    }

    public List<PeerAddress> getAddresses() {
        return addresses;
    }

    @Override
    public String getCommand() {
        return "addr";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        long count = leis.readVariableSize();
        for(long i = 0; i < count; i++)
        {
            int timestamp = leis.readInt();
            PeerAddress addr = new PeerAddress();
            addr.read(leis);
            addr.setTime(timestamp);
            addresses.add(addr);
        }
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeVariableSize(addresses.size());
        for(PeerAddress addr : addresses)
        {
            leos.writeInt(addr.getTime());
            addr.write(leos);
        }
    }
}
