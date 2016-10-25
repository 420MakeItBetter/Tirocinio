package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class PeerAddress implements BitSerializable {

    private long service;
    private int time;

    private InetAddress peerAddress;
    private int port;

    public PeerAddress(){}

    public void setService(long s){
        service = s;
    }

    public long getService() {
        return service;
    }

    public void setAddress(InetAddress addr){
        peerAddress = addr;
    }

    public void setPort(int p){
        port = p;
    }

    public void setTime(int t){
        time = t;
    }

    public int getTime() {
        return time;
    }

    /**
     * Reads the address structure without the Time Field
     * @param leis
     * @throws IOException
     */
    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        service = leis.readLong();
        byte [] addr = new byte[16];
        leis.read(addr);
        setAddress(InetAddress.getByAddress(addr));
        byte [] p = new byte[2];
        leis.read(p);
        setPort(( p[0]& 0xFF) << 8 | (p[1] & 0xFF));

    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeLong(service);
        if(peerAddress.getAddress().length == 4)
            leos.write(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                    (byte)0x00,(byte)0xFF,(byte)0xFF});
        leos.write(peerAddress.getAddress());
        leos.write(new byte[]{(byte)(port >> 8 & 0xFF), (byte)(port & 0xFF)});
    }

    @Override
    public String toString() {
        return " ip: "+ peerAddress.toString()+" port: "+port + " service: "+service;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return peerAddress;
    }
}
