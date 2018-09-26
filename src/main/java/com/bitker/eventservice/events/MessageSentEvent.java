package com.bitker.eventservice.events;

import com.bitker.network.Peer;
import com.bitkermessage.client.messages.messages.SerializedMessage;

/**
 * Created by machiara on 03/03/17.
 */
public class MessageSentEvent implements Event {

    public Peer p;
    public SerializedMessage msg;

    public MessageSentEvent(Peer p, SerializedMessage msg){
        this.p = p;
        this.msg = msg;
        if(msg.getCommand() != "inv")
            System.out.println("MessageSentEvent from: "+p.getAddress()+", sent "+msg.getCommand());
    }

}
