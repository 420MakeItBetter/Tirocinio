package Client.messages;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 31/10/2016.
 */
public class BufferStruct extends ConcurrentLinkedQueue<ByteBuffer> {

    private ConcurrentLinkedQueue<ByteBuffer> queue;
    private AtomicInteger dirty;

    public BufferStruct(){
        queue = new ConcurrentLinkedQueue<>();
        dirty = new AtomicInteger();
    }


    @Override
    public ByteBuffer poll() {
        dirty.set(1);
        return queue.poll();
    }

    @Override
    public ByteBuffer peek() {
        dirty.set(1);
        return  queue.peek();
    }

    @Override
    public boolean add(ByteBuffer buffer) {
        return queue.add(buffer);
    }

    public int garbageCollect(){
        if(dirty.get() == 1)
            dirty.set(0);
        else
        {
            ConcurrentLinkedQueue q = queue;
            queue = null;
            return q.size();
        }
        return 0;
    }
}
