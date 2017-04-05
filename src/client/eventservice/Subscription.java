package client.eventservice;

import client.eventservice.events.Event;
import client.eventservice.filters.Filter;
import client.eventservice.subscribers.Subscriber;

/**
 * Created by machiara on 03/03/17.
 */
public class Subscription<T extends Class<? extends Event>>{

    public T eventType;
    public Filter filter;
    public Subscriber subscriber;

    public Subscription(T eventType, Filter filter, Subscriber subscriber){
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
