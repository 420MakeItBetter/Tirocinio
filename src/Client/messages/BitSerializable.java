package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 07/10/2016.
 */
public interface BitSerializable {

    void read(LittleEndianInputStream leis) throws IOException;
    void write(LittleEndianOutputStream leos) throws IOException;
}
