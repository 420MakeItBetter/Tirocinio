package client.eventservice.subscribers;

import client.Main;
import client.protocol.ProtocolUtil;
import client.eventservice.EventService;
import client.eventservice.events.Event;
import client.eventservice.events.PeerStateChangedEvent;
import client.eventservice.filters.PeerFilter;
import client.network.Peer;
import client.network.PeerState;

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
