package Client.eventservice.subscribers;

import Client.eventservice.EventService;
import Client.eventservice.events.Event;
import Client.eventservice.events.PeerStateChangedEvent;
import Client.network.PeerState;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by machiara on 07/03/17.
 */
public class PeerStateSubscriber extends Subscriber {


    @Override
    public void inform(Event event) {

        if (event instanceof PeerStateChangedEvent)
        {
            PeerStateChangedEvent e = (PeerStateChangedEvent) event;
            ByteBuffer msg = ByteBuffer.allocate(4 + 4 + 8 + 16 + 1 + 1);
            msg.putInt(4 + 8 + 16 + 1 + 1);
            msg.putInt(5);
            msg.putLong(id);
            msg.put(e.p.getAddress().getAddress());
            msg.put(stateToByte(e.oldState));
            msg.putInt(stateToByte(e.p.getState()));
            data.addMsg(msg);
            if(e.p.getState() == PeerState.CLOSE)
                EventService.getInstance().unsubscribe(data,id);
        }
    }

    private byte stateToByte(PeerState state) {
        switch (state)
        {
            case CLOSE: return 0;
            case HANDSHAKE: return 1;
            case OPEN: return  2;
        }
        return -1;
    }
}
