package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;
import io.nayuki.bitcoin.crypto.Sha256Hash;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 *
 */
public class Header extends Message{

    private int version;
    private byte [] previous;
    private byte [] merkle;
    private int timestamp;
    private int bits;
    private int nonce;
    private int count;


    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setPrevious(byte[] previous) {
        this.previous = previous;
    }

    public void setPrevious(Sha256Hash previous){
        this.previous = previous.toBytes();
    }

    public byte[] getPrevious() {
        return previous;
    }

    public void setMerkle(byte[] merkle) {
        this.merkle = merkle;
    }

    public void setMerkle(Sha256Hash merkle){
        this.merkle = merkle.toBytes();
    }

    public byte[] getMerkle() {
        return merkle;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getBits() {
        return bits;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getNonce() {
        return nonce;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String getCommand() {
        return "headers";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        version = leis.readInt();
        previous = new byte [Sha256Hash.HASH_LENGTH];
        leis.read(previous);
        merkle = new byte [Sha256Hash.HASH_LENGTH];
        leis.read(merkle);
        timestamp = leis.readInt();
        bits = leis.readInt();
        nonce = leis.readInt();
        count = (int) leis.readVariableSize();
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeInt(version);
        leos.write(previous);
        leos.write(merkle);
        leos.writeInt(timestamp);
        leos.writeInt(bits);
        leos.writeInt(nonce);
        leos.writeVariableSize(count);
    }
}
