package Client.messages;

/**
 * Created by Matteo on 06/10/2016.
 */
public abstract class Message implements BitSerializable{

    public abstract String getCommand();
    private int checksum;
    private long length;

    public long getLength(){
        return length;
    }

    public void setLength(long length){
        this.length = length;
    }

    public void setChecksum(int c){
        checksum = c;
    }

    public int getChecksum(){
        return checksum;
    }

    @Override
    public String toString() {
        return "Message: "+getCommand()+"\nLength: "+getLength()+"\nChecksum: "+getChecksum();
    }

}
