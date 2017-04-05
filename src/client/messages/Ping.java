package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 */
public class Ping extends Message {

    private long nonce;

    public Ping(){
        setLength(8);
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }


    @Override
    public String getCommand() {
        return "ping";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        nonce = leis.readLong();
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeLong(nonce);
    }

    public long getNonce() {
        return nonce;
    }
}
