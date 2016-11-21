package Client;

import Client.commands.Command;
import Client.commands.Exit;
import Client.network.Peer;
import Client.network.PeerState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matteo on 14/11/2016.
 */
public class CommanderListener implements Runnable {


    @Override
    public void run() {
        try (ServerSocket skt = new ServerSocket(4201))
        {
            while(true)
            {
                Socket s = skt.accept();
                Thread t = new Thread(new CommandExecutor(s));
                t.start();
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

            List<InetAddress> peers = new LinkedList<>();
            for(Peer p : Main.peers.values())
                if(p.getState() == PeerState.OPEN)
                    peers.add(p.getAddress());
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(skt.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(skt.getInputStream());
                out.writeObject(peers);
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
                    command.execute(out);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                skt.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
