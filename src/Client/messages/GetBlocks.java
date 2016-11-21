package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;
import io.nayuki.bitcoin.crypto.Sha256Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class GetBlocks extends Message {

    private int version;
    private List<byte []> hashes;

    public GetBlocks(){
        hashes = new ArrayList<>();
    }

    public List<byte []> getHashes() {
        return hashes;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getCommand() {
        return "getblocks";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        version = leis.readInt();
        long hash_count = leis.readVariableSize();
        for(long i = 0; i < hash_count; i++)
        {
            byte [] b = new byte [Sha256Hash.HASH_LENGTH];
            leis.read(b);
            hashes.add(b);
        }
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeInt(version);
        leos.writeVariableSize(hashes.size());
        for(byte [] hash : hashes)
        {
            leos.write(hash);
        }
    }
}
