package Client;

import Client.network.Peer;
import Client.network.PeerState;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by machiara on 20/10/16.
 * 
 */
public class MainThread implements Runnable {


    Thread listener;

    MainThread(Thread t){
        listener = t;
    }

    @Override
    public void run() {
        Scanner s = new Scanner(System.in);
        boolean exit = false;
        while(!exit)
        {
            String command = s.next();
            switch (command)
            {
                case "exit" :
                    exit = true;
                    break;
                case "stat" :
                    int open = 0;
                    int handshake = 0;
                    int close = 0;
                    for(Peer p : Main.peers.values())
                        switch (p.getPeerState())
                        {
                            case OPEN:
                                open++;
                                break;
                            case HANDSAKE:
                                handshake++;
                                break;
                            case CLOSE:
                                close++;
                                break;
                        }
                    System.out.println("Connessioni aperte: "+open+"\nConnessioni in fase di handshake: "+handshake+"\nConnessioni chiuse: "+close+"\nConnessioni totali:"+(open+handshake+close));
                default:
                    break;

            }
        }
        listener.interrupt();
        File addresses = new File("./addresses.dat");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(addresses));
            System.out.println("scrivo "+Main.peers.size()+" indirizzi");
            for(Peer p : Main.peers.values())
            {
                out.write(p.getAddress().getHostAddress()+"\n");
                out.write(p.getPort()+"\n");
                out.write(p.getTimestamp()+"\n");
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
