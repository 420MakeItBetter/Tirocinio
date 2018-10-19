package com.bitker.eventservice.subscribers;

import com.bitker.api.ApiClientData;
import com.bitker.eventservice.events.Event;

/**
 * Created by machiara on 03/03/17.
 */
public abstract class Subscriber {

    public long id;
    public ApiClientData data;

    public abstract void inform(Event event);

    @Override
    public boolean equals(Object obj) {
        if(!obj.getClass().isAssignableFrom(Subscriber.class))
            return false;
        Subscriber o = (Subscriber) obj;
        return o.data.equals(data) && o.id == id;

    }
}

