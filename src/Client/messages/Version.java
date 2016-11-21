package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 07/10/2016.
 */
public class Version extends Message{


    private int version;
    private long services;
    private long timestamp;
    private PeerAddress myAddress;
    private PeerAddress yourAddress;
    private long nonce;
    private String userAgent;
    private int height;
    private boolean relay;

    public Version(){
        setLength(4 + 8 + 8 + 26 + 26 + 8 + 19 + 4 + 1);
    }

    @Override
    public String getCommand() {
        return "version";
    }

    public void setVersion(int v){
        version = v;
        if(version < 70001)
            setLength(4 + 8 + 8 + 26 + 26 + 8 + 19 + 4);
    }

    public void setServices(long s){
        services = s;
    }

    public void setTimestamp(long t){
        timestamp = t;
    }

    public void setMyAddress(PeerAddress addr){
        myAddress = addr;
    }

    public PeerAddress getMyAddress() {
        return myAddress;
    }

    public PeerAddress getYourAddress() {
        return yourAddress;
    }

    public void setYourAddress(PeerAddress addr){
        yourAddress = addr;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public long getNonce() {
        return nonce;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setRelay(boolean relay) {
        this.relay = relay;
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        version = leis.readInt();
        services = leis.readLong();
        timestamp = leis.readLong();
        myAddress = new PeerAddress();
        myAddress.read(leis);
        yourAddress = new PeerAddress();
        yourAddress.read(leis);
        nonce = leis.readLong();
        userAgent = leis.readString();
        height = leis.readInt();
        if(version >= 70001)
            relay = leis.readBoolean();
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeInt(version);
        leos.writeLong(services);
        leos.writeLong(timestamp);
        yourAddress.write(leos);
        myAddress.write(leos);
        leos.writeLong(nonce);
        leos.writeString(userAgent);
        leos.writeInt(height);
        if(this.version >= 70001)
            leos.writeBoolean(relay);
    }

    @Override
    public String toString() {
        return super.toString()+"[protocol: "+version+" service: "+services+" timestamp: "+ timestamp+ myAddress.toString()+ yourAddress.toString() + " nonce: "+nonce+" userAgent: "+userAgent + "height: "+height+" relay: "+relay+"]";
    }

    public long getService() {
        return services;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
