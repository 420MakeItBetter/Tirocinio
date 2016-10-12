package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;
import io.nayuki.bitcoin.crypto.Sha256Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matteo on 07/10/2016.
 */
public class GetBlocks extends Message {

    private int version;
    private List<Sha256Hash> hashes;

    public GetBlocks(){
        hashes = new ArrayList<>();
    }

    public List<Sha256Hash> getHashes() {
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
            Sha256Hash hash = new Sha256Hash(b);
            hashes.add(hash);
        }
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeInt(version);
        leos.writeVariableSize(hashes.size());
        for(Sha256Hash hash : hashes)
        {
            leos.write(hash.toBytes());
        }
    }
}
