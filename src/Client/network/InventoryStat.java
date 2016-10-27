package Client.network;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 25/10/2016.
 */
public class InventoryStat {

    public AtomicInteger error;
    public AtomicInteger transiction;
    public AtomicInteger block;
    public AtomicInteger filtered_block;
    public AtomicInteger cmpct_block;

    public InventoryStat(){
        error = new AtomicInteger();
        transiction = new AtomicInteger();
        block = new AtomicInteger();
        filtered_block = new AtomicInteger();
        cmpct_block = new AtomicInteger();
    }

}
