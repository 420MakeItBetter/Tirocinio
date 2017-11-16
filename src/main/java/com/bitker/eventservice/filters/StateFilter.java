package com.bitker.eventservice.filters;


import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitker.network.PeerState;

/**
 * Created by matteo on 21/04/17.
 */
public class StateFilter implements Filter {

    private PeerState state;

    public StateFilter(PeerState state){
        this.state = state;
    }

    @Override
    public boolean apply(Event e) {
        return  (e instanceof PeerStateChangedEvent && ((PeerStateChangedEvent) e).p.getState() == state);
    }
}
