package com.bitker.eventservice.subscribers;

import com.bitker.Main;
import com.bitker.protocol.ProtocolUtil;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitker.eventservice.filters.PeerFilter;
import com.bitker.network.Peer;
import com.bitker.network.PeerState;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Set;

/**
 * Created by machiara on 08/03/17.
 */
public class ChangePeerSubscriber extends Subscriber {

    public Set<String> peers;
    public ByteBuffer header;
    public ByteBuffer payload;
    public long reqid;

    @Override
    public void inform(Event event) {
        if(event instanceof PeerStateChangedEvent)
        {
            header.clear();
            payload.clear();
            for (Peer p : Main.peers.values())
                if (p.getState() == PeerState.OPEN && !peers.contains(p.getAddress().getHostAddress()))
                {
                    ByteBuffer h = ByteBuffer.allocate(24);
                    h.put(header);
                    ByteBuffer pa = ByteBuffer.allocate(payload.remaining());
                    pa.put(payload);
                    peers.remove(((PeerStateChangedEvent) event).p.getAddress().getHostAddress());
                    peers.add(p.getAddress().getHostAddress());
                    EventService.getInstance().unsubscribe(data,id);
                    EventService.getInstance().subscribe(PeerStateChangedEvent.class,new PeerFilter(peers),this);
                    try
                    {
                        ProtocolUtil.sendMessage(h, pa, p.getSocket(), p, reqid);
                    } catch (ClosedChannelException e)
                    {
                        e.printStackTrace();
                    }
                }
        }

    }
}
