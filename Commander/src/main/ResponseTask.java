package main;

/**
 * Created by machiara on 22/11/16.
 */
public abstract class ResponseTask implements Runnable {

    abstract void setResponse(Object o);
}
