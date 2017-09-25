package client.eventservice.subscribers;

import client.eventservice.EventService;
import client.eventservice.events.ConnectedEvent;
import client.eventservice.events.Event;
import client.eventservice.events.NotConnectedEvent;

import java.nio.ByteBuffer;

/**
 * Created by machiara on 07/03/17.
 */
public class ConnectSubscriber extends Subscriber {


    @Override
    public void inform(Event event) {
        ByteBuffer msg = ByteBuffer.allocate(4+4+8+16+1);
        msg.putInt(4+8+16+1);
        msg.putInt(6);
        msg.putLong(id);
        if(event instanceof ConnectedEvent)
        {
            if(((ConnectedEvent) event).p.getAddress().getAddress().length == 4)
                msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0xFF,(byte)0xFF});
            msg.put(((ConnectedEvent) event).p.getAddress().getAddress());
            msg.put((byte) 1);
        }
        if(event instanceof NotConnectedEvent)
        {
            if(((NotConnectedEvent) event).p.getAddress().getAddress().length == 4)
                msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0xFF,(byte)0xFF});
            msg.put(((NotConnectedEvent) event).p.getAddress().getAddress());
            msg.put((byte) 0);
            EventService.getInstance().unsubscribe(data,id);
        }
        data.addMsg(msg);
    }
}
