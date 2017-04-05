package client.eventservice.events;

import client.messages.SerializedMessage;
import client.network.Peer;

/**
 * Created by machiara on 03/03/17.
 */
public class MessageSentEvent implements Event {

    public Peer p;
    public SerializedMessage msg;

    public MessageSentEvent(Peer p, SerializedMessage msg){
        this.p = p;
        this.msg = msg;
    }

}
