package client.eventservice.subscribers;

import client.eventservice.events.ConnectedEvent;
import client.eventservice.events.Event;

import java.io.PrintWriter;

/**
 * Created by matteo on 13/04/17.
 */
public class ConnectionLogger extends SubscriberLogger {


    public ConnectionLogger(PrintWriter p) {
        super(p);
    }

    @Override
    public void inform(Event event) {
        if(event instanceof ConnectedEvent)
        {
            ConnectedEvent e = (ConnectedEvent) event;
            println("CONNECTED to: "+e.p.getAddress().getHostAddress()+ (e.p.isIn() ? "incoming connection " : "outgoing connection"));
        }
    }
}
