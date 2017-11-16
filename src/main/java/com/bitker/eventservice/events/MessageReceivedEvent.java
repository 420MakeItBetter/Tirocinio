package com.bitker.eventservice.events;


import com.bitker.messages.Message;
import com.bitker.network.Peer;

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
