package com.bitker.eventservice.filters;

import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.events.MessageReceivedEvent;
import com.bitker.eventservice.events.MessageSentEvent;

import java.util.Set;

/**
 * Created by machiara on 07/03/17.
 */
public class MsgFilter implements Filter {

    Set<String> msgtypes;
    long id;

    public MsgFilter(Set<String> msgtypes) {
        this.msgtypes = msgtypes;
    }

    public MsgFilter(long id){
        this.id = id;
    }

    @Override
    public boolean apply(Event e) {
        return (e instanceof MessageReceivedEvent && msgtypes.contains(((MessageReceivedEvent) e).m.getCommand()))
                ||
                (e instanceof MessageSentEvent && ((MessageSentEvent) e).msg.getId() == id);
    }

}
