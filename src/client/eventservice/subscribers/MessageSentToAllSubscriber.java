package client.eventservice.subscribers;

import client.eventservice.events.Event;
import client.eventservice.events.MessageSentEvent;

import java.nio.ByteBuffer;

/**
 * Created by machiara on 08/03/17.
 */
public class MessageSentToAllSubscriber extends Subscriber {

    public int n;

    private int actual;

    public MessageSentToAllSubscriber(){
        actual = 0;
    }

    @Override
    public void inform(Event event) {
        if(event instanceof MessageSentEvent)
        {
            actual++;
            if(n == actual)
            {
                ByteBuffer msg = ByteBuffer.allocate(4+4+8);
                msg.putInt(4+8);
                msg.putInt(3);
                msg.putLong(id);
                data.addMsg(msg);
            }
        }
    }
}
