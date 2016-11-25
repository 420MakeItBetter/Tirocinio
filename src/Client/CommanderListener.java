package Client;

import Client.commands.AddrStruct;
import Client.commands.Command;
import Client.commands.Exit;
import Client.messages.Address;
import Client.network.AddressData;
import Client.network.Peer;
import Client.network.PeerState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Matteo on 14/11/2016.
 */
public class CommanderListener implements Runnable {

    public BlockingQueue<String> messages;

    public BlockingQueue<AddressData> addressess;

    public AtomicBoolean connected;

    @Override
    public void run() {
        messages = new LinkedBlockingQueue<>();
        addressess = new LinkedBlockingQueue<>();
        connected = new AtomicBoolean(false);
        try (ServerSocket skt = new ServerSocket(4201))
        {
            try(ServerSocket skt1 = new ServerSocket(5000))
            {
                try (ServerSocket skt2 = new ServerSocket(5001))
                {
                    while (true)
                    {
                        Socket s = skt.accept();
                        Socket s1 = skt1.accept();
                        Socket s2 = skt2.accept();
                        new Thread(new CommandExecutor(s)).start();
                        new Thread(new MessageSender(s1)).start();
                        new Thread(new AddressSender(s2)).start();
                        connected.set(true);
                    }
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    private class CommandExecutor implements Runnable {

        Socket skt;

        CommandExecutor(Socket s) {
            skt = s;
        }

        @Override
        public void run() {
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(skt.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(skt.getInputStream());
                Command command = null;
                while(true)
                {
                    try
                    {
                        command = (Command) in.readUnshared();
                    } catch (ClassNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    if(command instanceof Exit)
                        break;
                    System.out.println("Eseguo comando");
                    command.execute(out);
                }
            } catch (IOException e)
            {
            }
            try
            {
                skt.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            Main.followed = null;
            connected.set(false);
        }
    }

    private class MessageSender implements Runnable {

        Socket skt;

        MessageSender(Socket s){
            this.skt = s;
        }

        @Override
        public void run() {
            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(skt.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true){
                try {
                   String str = messages.take();
                    out.writeUnshared(str);
                    System.out.println("Scrivo messaggio");
                } catch (InterruptedException e) {
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    private class AddressSender implements Runnable{

        Socket skt;

        AddressSender(Socket s){
            this.skt = s;
        }

        @Override
        public void run() {
            ObjectOutputStream out = null;
            try
            {
                out = new ObjectOutputStream(skt.getOutputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            while(true)
            {
                try
                {
                    AddressData addr = addressess.take();
                    out.writeUnshared(addr.m);
                    out.writeUnshared(addr.p.getAddress().getHostAddress());
                    System.out.println("Scrivo indirizzo");
                } catch (InterruptedException e){
                } catch (IOException e)
                {
                    break;
                }
            }
        }
    }
}
