package Client;

import Client.messages.SerializedMessage;
import Client.network.Peer;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Matteo on 02/11/2016.
 */
public class ExternalListener implements Runnable {


    @Override
    public void run() {
        try
        {
            ServerSocket server = new ServerSocket(4200);
            long start = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted())
            {
                Socket skt = server.accept();
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