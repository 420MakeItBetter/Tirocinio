package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 */
public class TransactionOutput implements BitSerializable{

    private long value;
    private byte [] script;

    public void setValue(long value) {
        this.value = value;
    }

    public void setScript(byte[] script) {
        this.script = script;
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        value = leis.readLong();
        int length = (int) leis.readVariableSize();
        script = new byte [length];
        leis.read(script);
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.writeLong(value);
        leos.writeVariableSize(script.length);
        leos.write(script);
    }
}
