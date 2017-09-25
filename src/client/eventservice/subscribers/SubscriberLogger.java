package client.eventservice.subscribers;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by matteo on 13/04/17.
 */
public abstract class SubscriberLogger extends Subscriber {

    private PrintWriter p;
    protected SimpleDateFormat sdf;

    SubscriberLogger(PrintWriter p){
        this.p = p;
        sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm ss.SSSS");
    }

    protected void println(String s){
        p.println(sdf.format(new Date())+" "+s);

    }

}
