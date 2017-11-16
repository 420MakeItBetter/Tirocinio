package com.bitker.eventservice.subscribers;

import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitker.network.PeerState;

import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by matteo on 13/04/17.
 */
public class PeerStateLogger extends SubscriberLogger {

    public PeerStateLogger(PrintWriter p) {
        super(p);
    }

    @Override
    public void inform(Event event) {
        if(event instanceof PeerStateChangedEvent)
        {
            PeerStateChangedEvent e = (PeerStateChangedEvent) event;
            if (e.p.getState() == PeerState.CLOSE)
            {
                Date d = new Date(System.currentTimeMillis() - e.p.getConnectionTime());
                println("DISCONNECTED from: " + e.p.getAddress().getHostAddress() + " after being connected for: "+sdf.format(d)+" before was: "+stateToString(e.oldState));
            }
            else
                println("STATE CHANGED for: "+e.p.getAddress().getHostAddress()+ " before was: "+stateToString(e.oldState));
        }
    }

    private String stateToString(PeerState oldState) {
        switch (oldState)
        {
            case HANDSHAKE: return "handshake";
            case CLOSE: return "close";
            case OPEN: return "open";
        }
        return null;
    }
}
