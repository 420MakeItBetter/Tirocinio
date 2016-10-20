package Client;

import Client.network.SocketListener;

/**
 * Created by Matteo on 18/10/2016.
 */
public class Test {


    public static SocketListener l = new SocketListener();

    public static void main(String [] args){
        Thread t = new Thread(l);
        t.start();
    }
}
