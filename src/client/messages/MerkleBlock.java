package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;
import io.nayuki.bitcoin.crypto.Sha256Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 08/10/2016.
 */
public class MerkleBlock extends Message {

    private int version;
    private byte [] previous;
    private byte [] merkle;
    private int timestamp;
    private int bits;
    private int nonce;
    private int totaltransaction;
    private List<byte []> hashes;
    private byte [] flags;

    public MerkleBlock(){
        hashes = new ArrayList<>();
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setPrevious(byte[] previous) {
        this.previous = previous;
    }

    public void setPrevious(Sha256Hash previous){
        this.previous = previous.toBytes();
    }

    public void setMerkle(byte [] merkle){
        this.merkle = merkle;
    }

    public void setMerkle(Sha256Hash merkle){
        this.merkle = merkle.toBytes();
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public void setTotaltransaction(int totaltransaction) {
        this.totaltransaction = totaltransaction;
    }

    public List<byte []> getHashes() {
        return hashes;
    }

    public void setFlags(byte[] flags) {
        this.flags = flags;
    }

    @Override
    public String getCommand() {
        return "merkleblock";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        version = leis.readInt();
        previous = new byte [32];
        leis.read(previous);
        merkle = new byte [32];
        leis.read(merkle);
        timestamp = leis.readInt();
        bits = leis.readInt();
        nonce = leis.readInt();
        totaltransaction = leis.readInt();
        /*
         Da capire meglio cosa e' uint256[]
         */
        long length = leis.readVariableSize();
        for(long i = 0; i < length; i++)
        {
            byte [] hash = new byte [32];
            leis.read(hash);
            hashes.add(hash);
        }
        int len = (int) leis.readVariableSize();
        flags = new byte [len];
        leis.read(flags);
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeInt(version);
        leos.write(previous);
        leos.write(merkle);
        leos.writeInt(timestamp);
        leos.writeInt(bits);
        leos.writeInt(nonce);
        leos.writeInt(totaltransaction);
        /*
         Da capire meglio cosa e' uint256[]
         */
        leos.writeVariableSize(hashes.size());
        for(byte [] hash : hashes)
            leos.write(hash);
        leos.writeVariableSize(flags.length);
        leos.write(flags);
    }
}
