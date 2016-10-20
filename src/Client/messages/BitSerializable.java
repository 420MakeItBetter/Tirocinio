package Client.messages;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * This is an interface that all the structures that can be serialized as Bitcoin Protocol structures need to implements
 *
 * @author Matteo Franceschi
 */
public interface BitSerializable {

    void read(LittleEndianInputStream leis) throws IOException;
    void write(LittleEndianOutputStream leos) throws IOException;
}
