package Client.network;

import Client.messages.SerializedMessage;

/**
 * Created by Matteo on 01/11/2016.
 */
public class MemReturner implements Runnable {
    SerializedMessage msg;

    public MemReturner(SerializedMessage msg){
        this.msg = msg;
    }

    @Override
    public void run() {
        try
        {
            SerializedMessage.returnHeader(msg.getHeader());
            SerializedMessage.returnPayload(msg.getPayload());
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
