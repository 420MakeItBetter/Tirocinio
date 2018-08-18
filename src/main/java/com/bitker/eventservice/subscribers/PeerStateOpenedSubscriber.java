package com.bitker.eventservice.subscribers;

import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitker.network.PeerState;
import com.bitker.protocol.ProtocolUtil;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

public class PeerStateOpenedSubscriber extends Subscriber {

    public ByteBuffer header;
    public ByteBuffer payload;

    @Override
    public void inform(Event event) {

        if (event instanceof PeerStateChangedEvent)
        {
            PeerStateChangedEvent e = (PeerStateChangedEvent) event;
            if(e.p.getState() == PeerState.OPEN)
            {
                try {
                    ProtocolUtil.sendMessage(header,payload,e.p.getSocket(),e.p,id);
                } catch (ClosedChannelException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
