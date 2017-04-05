package client.eventservice.filters;

import client.eventservice.events.Event;

/**
 * Created by machiara on 03/03/17.
 */
public interface Filter {
    boolean apply(Event e);
}
