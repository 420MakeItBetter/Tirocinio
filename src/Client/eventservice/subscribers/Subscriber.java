package Client.eventservice.subscribers;

import Client.api.ApiClientData;
import Client.eventservice.events.Event;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

/**
 * Created by machiara on 03/03/17.
 */
public abstract class Subscriber {

    public long id;
    public ApiClientData data;

    public abstract void inform(Event event);

    @Override
    public boolean equals(Object obj) {
        Subscriber o = (Subscriber) obj;
        return o.data.equals(data) && o.id == id;

    }
}

