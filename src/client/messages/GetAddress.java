package client.messages;

import client.bitio.LittleEndianInputStream;
import client.bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 08/10/2016.
 *
 */
public class GetAddress extends Message {


    public GetAddress(){
        setLength(0);
    }

    @Override
    public String getCommand() {
        return "getaddr";
    }

    @Override
    public void read(LittleEndianInputStream leis) throws IOException {

    }

    @Override
    public void write(LittleEndianOutputStream leos) throws IOException {

    }
}
