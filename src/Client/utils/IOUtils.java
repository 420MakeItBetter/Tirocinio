package Client.utils;

import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;
import Client.messages.Message;

import java.io.IOException;

/**
 * Created by Matteo on 11/10/2016.
 */
public class IOUtils {

    public static void read(LittleEndianInputStream leis, Message m) throws IOException {
        int magic = leis.readInt();
        byte [] command = new byte [12];
        leis.read(command);
        m.setLength(leis.readInt());
        m.setChecksum(leis.readInt());
        m.read(leis);
    }

    public static void write(LittleEndianOutputStream leos, Message m) throws IOException{
        leos.writeInt(0xDAB5BFFA);
        byte [] command = new byte [12];
        byte [] com = m.getCommand().getBytes();
        for(int i = 0; i < com.length; i++)
            command[i] = com[i];
        leos.writeInt(m.getLength());
        leos.writeInt(m.getChecksum());
        m.write(leos);
    }

}
