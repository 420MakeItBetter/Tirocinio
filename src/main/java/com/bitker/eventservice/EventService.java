package com.bitker.eventservice;

import com.bitker.api.ApiClientData;
import com.bitker.eventservice.filters.Filter;
import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.subscribers.Subscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.HashSet;
import java.util.Set;

/**
 * Created by machiara on 03/03/17.
 */
public class EventService {

    private static final EventService instance = new EventService();

    private final Object MONITOR;

    private Set<Subscription> subscribers;

    private EventService(){
        MONITOR = new Object();
    }

    public static EventService getInstance(){
        return instance;
    }



    public <T extends Class<? extends Event>> void subscribe(@NotNull T eventType, @Nullable Filter filter, @NotNull Subscriber subscriber){
        Subscription<T> subscription = new Subscription<>(eventType,filter,subscriber);
        synchronized (MONITOR){
            if(subscribers == null)
                subscribers = new HashSet<>(1);
            subscribers.add(subscription);
        }
    }


    public void unsubscribe(@NotNull ApiClientData data, long id){
        synchronized (MONITOR){
            if(subscribers == null)
                return;
            subscribers.removeIf(s -> s.subscriber.id == id && s.subscriber.data.equals(data));
        }
    }

    public void publish(Event e){
        HashSet<Subscription> tmp = null;
        synchronized (MONITOR){
            if(subscribers == null)
                return;
           tmp = new HashSet<>(subscribers);
        }

        for(Subscription s : tmp)
            if(s.eventType.isAssignableFrom(e.getClass()) && (s.filter == null || s.filter.apply(e)))
            {
                s.subscriber.inform(e);
            }
    }

}
