package com.bitker.eventservice.events;

import com.bitker.network.Peer;

/**
 * Created by machiara on 06/03/17.
 */
public class NotConnectedEvent implements Event {

    public Peer p;

    public NotConnectedEvent(Peer p) {
        this.p = p;
    }
}
