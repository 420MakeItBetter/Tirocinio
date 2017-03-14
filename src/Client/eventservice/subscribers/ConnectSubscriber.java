package Client.eventservice.subscribers;

import Client.eventservice.EventService;
import Client.eventservice.events.ConnectedEvent;
import Client.eventservice.events.Event;
import Client.eventservice.events.NotConnectedEvent;

import java.nio.ByteBuffer;

/**
 * Created by machiara on 07/03/17.
 */
public class ConnectSubscriber extends Subscriber {


    @Override
    public void inform(Event event) {
        ByteBuffer msg = ByteBuffer.allocate(4+4+8+1);
        msg.putInt(4+8+1);
        msg.putInt(6);
        msg.putLong(id);
        if(event instanceof ConnectedEvent)
        {
            msg.put(((ConnectedEvent) event).p.getAddress().getAddress());
            msg.put((byte) 1);
        }
        if(event instanceof NotConnectedEvent)
        {
            msg.put(((NotConnectedEvent) event).p.getAddress().getAddress());
            msg.put((byte) 0);
            EventService.getInstance().unsubscribe(data,id);
        }
        data.addMsg(msg);
    }
}
