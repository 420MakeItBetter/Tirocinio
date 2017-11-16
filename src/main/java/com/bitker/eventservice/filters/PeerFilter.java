package com.bitker.eventservice.filters;

import com.bitker.eventservice.events.*;

import java.util.Set;

/**
 * Created by machiara on 07/03/17.
 */
public class PeerFilter implements Filter {

    Set<String> peers;

    public PeerFilter(Set<String> peers) {
        this.peers = peers;
    }

    @Override
    public boolean apply(Event e) {
        return  (e instanceof MessageReceivedEvent && peers.contains(((MessageReceivedEvent) e).p.getAddress().getHostAddress()))
                ||
                (e instanceof PeerStateChangedEvent && peers.contains(((PeerStateChangedEvent) e).p.getAddress().getHostAddress()))
                ||
                (e instanceof ConnectedEvent && peers.contains(((ConnectedEvent) e).p.getAddress().getHostAddress()))
                ||
                (e instanceof NotConnectedEvent && peers.contains(((NotConnectedEvent) e).p.getAddress().getHostAddress()))
                ||
                (e instanceof MessageSentEvent && peers.contains(((MessageSentEvent) e).p.getAddress().getHostAddress()));
    }

}
