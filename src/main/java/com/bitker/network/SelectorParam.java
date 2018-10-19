package com.bitker.network;

import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;

/**
 * Created by Matteo on 17/10/2016.
 *
 */
public class SelectorParam {

    private AbstractSelectableChannel channel;
    private int interest;
    private Peer o;

    public SelectorParam(AbstractSelectableChannel c,int i,Peer o){
        this.channel = c;
        interest = i;
        this.o = o;
    }

    public SelectionKey register(Selector selector) throws ClosedChannelException {
        SelectionKey k = channel.register(selector,interest,o);
        o.setKey(k);
        return k;
    }

    public Peer getInterest(){
        return  o;
    }

}
