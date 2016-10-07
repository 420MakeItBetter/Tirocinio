package messages;

import bitio.LittleEndianInputStream;
import bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 07/10/2016.
 */
public interface BitSerializable {

    void read(LittleEndianInputStream leis) throws IOException;
    void write(LittleEndianOutputStream leos) throws IOException;
}
