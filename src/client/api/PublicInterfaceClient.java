package client.api;

import java.net.Socket;

/**
 * Created by machiara on 03/03/17.
 */
public class PublicInterfaceClient implements Runnable {

    Socket skt;

    public PublicInterfaceClient(Socket s) {
        skt = s;
    }

    @Override
    public void run() {

    }
}
