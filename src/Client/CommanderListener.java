package Client;

import Client.commands.AddrStruct;
import Client.commands.Command;
import Client.commands.Exit;
import Client.messages.Address;
import Client.messages.SerializedMessage;
import Client.network.AddressData;
import Client.network.Peer;
import Client.network.PeerState;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Matteo on 14/11/2016.
 */
public class CommanderListener implements Runnable {


    public LinkedBlockingQueue<String> address;

    CommanderListener(){
        address = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {


        try (ServerSocket skt = new ServerSocket(4201))
        {
            while (true)
            {
                Main.client = skt.accept();
                new Thread(new AddrSender()).start();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private class AddrSender implements Runnable {
        @Override
        public void run() {

                OutputStreamWriter writer  = null;
            try
            {
                writer = new OutputStreamWriter(Main.client.getOutputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            while (true)
            {
                String s = address.poll();
                try
                {
                    writer.write(s);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
