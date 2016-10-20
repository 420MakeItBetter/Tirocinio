package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 */
public class Pong extends Message {

    private long nonce;

    public Pong(){
        setLength(8);
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public long getNonce() {
        return nonce;
    }

    @Override
    public String getCommand() {
        return "pong";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        nonce = leis.readLong();
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeLong(nonce);
    }
}
