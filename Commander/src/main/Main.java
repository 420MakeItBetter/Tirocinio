package main;

import Client.commands.Command;
import Client.commands.Exit;
import Client.commands.Send;
import Client.commands.Update;
import Client.messages.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Matteo on 15/11/2016.
 */
public class Main implements Runnable {

    public static Socket skt;
    public static BlockingQueue<CommandTask> commands;
    public static JFrame window;
    public static ObjectInputStream in;
    public static ObjectOutputStream out;
    public static Executor ex;

    public static Map<PeerAddress, InetAddress> addressSent;


    public static void main(String [] args) throws IOException, ClassNotFoundException {
        skt = new Socket();
        addressSent = new HashMap<>();
        commands = new LinkedBlockingQueue<>();
        SwingUtilities.invokeLater(new Main());
        ex = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        window = new ClientGui();
        new Thread(new Sender()).start();
    }


    private class Sender implements Runnable {
        @Override
        public void run() {
            while(true)
            {
                try {
                    CommandTask c = commands.take();
                    out.writeUnshared(c.command);
                    Object o = in.readUnshared();
                    c.task.setResponse(o);
                    ex.execute(c.task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
