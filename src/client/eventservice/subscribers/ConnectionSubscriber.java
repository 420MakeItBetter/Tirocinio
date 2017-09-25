package client.eventservice.subscribers;

import client.eventservice.events.ConnectedEvent;
import client.eventservice.events.Event;
import client.eventservice.events.NotConnectedEvent;

import java.nio.ByteBuffer;

/**
 * Created by matteo on 21/04/17.
 */
public class ConnectionSubscriber extends Subscriber {
    @Override
    public void inform(Event event) {
        ByteBuffer msg = ByteBuffer.allocate(4+4+8+16+1);
        msg.putInt(4+8+16+1);
        msg.putInt(7);
        msg.putLong(id);
        if(((ConnectedEvent) event).p.getAddress().getAddress().length == 4)
            msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                    (byte)0x00,(byte)0xFF,(byte)0xFF});
        msg.put(((ConnectedEvent) event).p.getAddress().getAddress());
        msg.put((((ConnectedEvent) event).p.isIn() ? (byte) 1 : (byte) 0));
        data.addMsg(msg);
    }
}
