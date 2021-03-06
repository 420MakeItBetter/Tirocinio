package com.bitker.eventservice;

import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.filters.Filter;
import com.bitker.eventservice.subscribers.Subscriber;

/**
 * Created by machiara on 03/03/17.
 */
public class Subscription<T extends Class<? extends Event>>{

    T eventType;
    public Filter filter;
    public Subscriber subscriber;

    Subscription(T eventType, Filter filter, Subscriber subscriber){
        this.eventType = eventType;
        this.filter = filter;
        this.subscriber = subscriber;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Subscription
                &&
                subscriber.equals(((Subscription) obj).subscriber)
                &&
                (filter == null || ((Subscription) obj).filter == null || filter.equals(((Subscription) obj).filter));
    }

}
