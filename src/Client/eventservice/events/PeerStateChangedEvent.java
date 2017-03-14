package Client.eventservice.events;

import Client.network.Peer;
import Client.network.PeerState;

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
