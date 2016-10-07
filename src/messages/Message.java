package messages;

import bitio.LittleEndianInputStream;
import bitio.LittleEndianOutputStream;

import java.io.IOException;

/**
 * Created by Matteo on 06/10/2016.
 */
public abstract class Message implements BitSerializable{

    public abstract String getCommand();
    private int length;
    private int checksum;

    public int getLength(){
        return length;
    }

    public void setLength(int l){
        length = l;
    }

    public void setChecksum(int c){
        checksum = c;
    }

    public int getChecksum(){
        return checksum;
    }






}
