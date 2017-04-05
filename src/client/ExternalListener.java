package client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by Matteo on 02/11/2016.
 */
public class ExternalListener implements Runnable {


    @Override
    public void run() {
        try
        {
            ServerSocket server = new ServerSocket(4200);
            server.setSoTimeout(1000*60);
            server.setReuseAddress(true);
            long start = System.currentTimeMillis();
            while (!Main.terminate.get())
            {
                try
                {
                    Socket skt = server.accept();
                    ClientHandler ch = new ClientHandler(skt, start);
                    Thread t = new Thread(ch);
                    t.start();
                }catch (SocketTimeoutException e)
                {}
            }
            server.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}