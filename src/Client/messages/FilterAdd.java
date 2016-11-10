package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 *
 */
public class FilterAdd extends Message{

    private byte [] data;

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String getCommand() {
        return "filteradd";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {
        data = new byte [(int) getLength()];
        leis.read(data);
    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {
        leos.write(data);
    }
}
