package client.eventservice.events;

import client.network.Peer;

/**
 * Created by machiara on 06/03/17.
 */
public class NotConnectedEvent implements Event {

    public Peer p;

    public NotConnectedEvent(Peer p) {
        this.p = p;
    }
}
