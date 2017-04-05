package client.eventservice.events;

import client.network.Peer;
import client.network.PeerState;

/**
 * Created by machiara on 03/03/17.
 */
public class PeerStateChangedEvent implements Event {

    public Peer p;
    public PeerState oldState;

    public PeerStateChangedEvent(Peer peer, PeerState oldState) {
        p = peer;
        this.oldState= oldState;
    }
}
