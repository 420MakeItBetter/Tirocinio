package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of the "addr" {@link Message}
 *
 * @author Matteo Franceschi
 * @see Message
 *
 */
public class Address extends Message {

    /**
     * Constant that indicates the max number of entries for a single message
     */
    private static final int MAX_ENTRIES = 1000;


    @Override
    public int getLength() {
        int varint = 1;
        if(addresses.size() < 0xFD)
            varint = 1;
        else if(addresses.size() <= 0xFFFF)
            varint = 3;
        else if(addresses.size() <= 0xFFFFFFFF)
            varint = 5;
        return 30*addresses.size() + varint;
    }

    /**
     * The list of {@link PeerAddress} of this message
     */
    private List<PeerAddress> addresses;

    /**
     * Constructor that simply create an {@link ArrayList} for the addresses
     */
    public Address(){
        addresses = new ArrayList<>();
    }

    /**
     * Getter for the list of address to manipulate it
     * @return the list of addresses
     */
    public List<PeerAddress> getAddresses() {
        return addresses;
    }

    /**
     * The command of this message
     * @return "addr"
     */
    @Override
    public String getCommand() {
        return "addr";
    }

    /**
     * initialize this message reading from an {@link LittleEndianInputStream}
     * @param leis the inputStream
     * @throws IOException in case something went wrong
     */
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

    /**
     * serialize accordingly to the Bitcoin Protocol this message to the {@link LittleEndianOutputStream} passed as argument
     *
     * @param leos the outputStream
     * @throws IOException in case something went wrong
     */
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
