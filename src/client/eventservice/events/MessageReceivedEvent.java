package client.eventservice.events;

import client.messages.Message;
import client.network.Peer;

/**
 * Created by machiara on 03/03/17.
 */
public class MessageReceivedEvent implements Event {

    public Peer p;
    public Message m;

    public MessageReceivedEvent(Peer p, Message m) {
        this.p = p;
        this.m = m;
    }
}
