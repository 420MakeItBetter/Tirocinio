package Client.network;

import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Objects;

/**
 * Created by Matteo on 17/10/2016.
 */
public class SelectorParam {

    private AbstractSelectableChannel channel;
    private int interest;
    private Object o;

    public SelectorParam(AbstractSelectableChannel c,int i,Object o){
        this.channel = c;
        interest = i;
        this.o = o;
    }

    public SelectionKey register(Selector selector) throws ClosedChannelException {
       return channel.register(selector,interest,o);
    }


}
