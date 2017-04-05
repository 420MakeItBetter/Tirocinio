package client.network;

import java.io.IOException;

/**
 * Created by machiara on 20/02/17.
 */
public abstract class Task implements Runnable {

    protected Peer p;

    @Override
    public void run() {
        try{
            doTask();
            clean();
        }catch (IOException e){
            //e.printStackTrace();
            closeResources();
        }

    }

    protected abstract void clean();

    protected abstract void closeResources();

    protected abstract void doTask() throws IOException;
}
