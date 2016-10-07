package messages;

import bitio.LittleEndianInputStream;
import bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 07/10/2016.
 */
public class Inventory extends Message {




    @Override
    public String getCommand() {
        return "inv";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {

    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {

    }
}
