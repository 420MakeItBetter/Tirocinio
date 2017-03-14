package Client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Matteo on 02/11/2016.
 */
public class ExternalListener implements Runnable {


    @Override
    public void run() {
        try
        {
            ServerSocket server = new ServerSocket(4200);
            Main.openedFiles.incrementAndGet();
            long start = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted())
            {
                Socket skt = server.accept();
                Main.openedFiles.incrementAndGet();
                ClientHandler ch = new ClientHandler(skt,start);
                Thread t = new Thread(ch);
                t.start();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}