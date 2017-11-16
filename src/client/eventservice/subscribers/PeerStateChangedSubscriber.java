package client.eventservice.subscribers;

import client.eventservice.events.Event;
import client.eventservice.events.PeerStateChangedEvent;
import client.network.PeerState;

import java.nio.ByteBuffer;

/**
 * Created by matteo on 21/04/17.
 */
public class PeerStateChangedSubscriber extends Subscriber {


    @Override
    public void inform(Event event) {
        if (event instanceof PeerStateChangedEvent)
        {
            PeerStateChangedEvent e = (PeerStateChangedEvent) event;
            ByteBuffer msg;
            if(e.p.getState() != PeerState.CLOSE)
                msg = ByteBuffer.allocate(4 + 4 + 8 + 16 + 1 + 1);
            else
                msg = ByteBuffer.allocate(4+4+8+16+1+1+8);
            msg.putInt(4 + 8 + 16 + 1 + 1);
            msg.putInt(5);
            msg.putLong(id);
            if (e.p.getAddress().getAddress().length == 4)
                msg.put(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0xFF, (byte) 0xFF});
            msg.put(e.p.getAddress().getAddress());
            msg.put(stateToByte(e.oldState));
            msg.put(stateToByte(e.p.getState()));
            if(e.p.getState() == PeerState.CLOSE)
                msg.putLong(System.currentTimeMillis() - e.p.getConnectionTime());
            data.addMsg(msg);
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