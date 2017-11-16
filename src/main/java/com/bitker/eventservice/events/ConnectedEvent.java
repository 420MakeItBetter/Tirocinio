package com.bitker.eventservice.events;

import com.bitker.network.Peer;

/**
 * Created by machiara on 03/03/17.
 */
public class ConnectedEvent implements Event{

    public Peer p;

    public ConnectedEvent(Peer p){
        this.p = p;
    }
}
