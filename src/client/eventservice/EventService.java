package client.eventservice;

import client.api.ApiClientData;
import client.eventservice.events.Event;
import client.eventservice.filters.Filter;
import client.eventservice.subscribers.Subscriber;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by machiara on 03/03/17.
 */
public class EventService {

    private static final EventService instance = new EventService();

    private final Object MONITOR;

    private final Executor ex;

    private Set<Subscription> subscribers;

    private EventService(){
        MONITOR = new Object();
        ex = Executors.newCachedThreadPool();
    }

    public static EventService getInstance(){
        return instance;
    }



    public <T extends Class<? extends Event>> void subscribe(@NotNull T eventType, @Nullable Filter filter, @NotNull Subscriber subscriber){
        Subscription<T> subscription = new Subscription(eventType,filter,subscriber);
        synchronized (MONITOR){
            if(subscribers == null)
                subscribers = new HashSet<>(1);
            subscribers.add(subscription);
        }
    }


    public void unsubscribe(@NotNull ApiClientData data,@NotNull long id){
        synchronized (MONITOR){
            if(subscribers == null)
                return;
            for(Subscription s : subscribers)
                if(s.subscriber.id == id && s.subscriber.data.equals(data))
                    subscribers.remove(s);
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
                    s.subscriber.inform(e);
    }

}
