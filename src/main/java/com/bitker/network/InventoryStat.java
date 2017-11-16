package com.bitker.network;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Matteo on 25/10/2016.
 */
public class InventoryStat {

    public AtomicLong error;
    public AtomicLong transiction;
    public AtomicLong block;
    public AtomicLong filtered_block;
    public AtomicLong cmpct_block;

    public InventoryStat(){
        error = new AtomicLong();
        transiction = new AtomicLong();
        block = new AtomicLong();
        filtered_block = new AtomicLong();
        cmpct_block = new AtomicLong();
    }

}
