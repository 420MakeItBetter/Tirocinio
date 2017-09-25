package client.eventservice.subscribers;

import client.eventservice.events.Event;
import client.eventservice.events.MessageReceivedEvent;
import client.eventservice.events.MessageSentEvent;
import client.messages.Version;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by matteo on 13/04/17.
 */
public class MessageLogger extends SubscriberLogger {


    public MessageLogger(PrintWriter p){
        super(p);
    }

    @Override
    public void inform(Event event) {
        if(event instanceof MessageSentEvent)
        {
            MessageSentEvent e = (MessageSentEvent) event;
            println("MSG SENT: "+e.msg.getCommand()+" to peer "+e.p.getAddress().getHostAddress());
        }
        else if(event instanceof MessageReceivedEvent)
        {
            MessageReceivedEvent e = (MessageReceivedEvent) event;
            if(e.m instanceof Version)
            {
                println("VERSION RECEIVED from "+e.p.getAddress().getHostAddress()+" agent: "+((Version) e.m).getUserAgent()+" service: "+((Version) e.m).getService()+" height: "+((Version) e.m).getHeight()+" protocol #: "+((Version) e.m).getVersion());
            }
            else
                println("MSG RECEIVED from "+e.p.getAddress().getHostAddress()+" type: "+e.m.getCommand());
        }
    }


}
