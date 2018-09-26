package com.bitker.eventservice.events;

import com.bitker.network.Peer;
import com.bitkermessage.client.messages.messages.Message;

/**
 * Created by machiara on 03/03/17.
 */
public class MessageReceivedEvent implements Event {

    public Peer p;
    public Message m;

    public MessageReceivedEvent(Peer p, Message m) {
        this.p = p;
        this.m = m;
        if(m.getCommand() == "getdata")
            System.out.println("MessageReceivedEvent from: "+p.getAddress()+", received "+m.getCommand());
    }
}
