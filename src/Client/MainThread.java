package Client;

import Client.messages.SerializedMessage;
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
        long start = System.currentTimeMillis();
        while(!exit)
        {
            String command = s.next();
            switch (command)
            {
                case "exit" :
                    exit = true;
                    break;
                case "uptime" :
                    long time = System.currentTimeMillis() - start;
                    System.out.println("Client in esecuzione da: \n"+time/(1000*60*60*24)+"G "+(time%(1000*60*60*24))/(1000*60*60)+"H "+(time%(1000*60*60))/(1000*60) +"m "+((time%(1000*60))/1000)+"s ");
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
                    break;
                case "inv" :
                    System.out.println("Errors:"+Main.invStat.error.get()+"\nTransactions:"+Main.invStat.transiction.get()+"\nBlocks:"+Main.invStat.block.get()+"\nFiltered Block:"+Main.invStat.filtered_block.get()+"\nCMPCT Blocks:"+Main.invStat.cmpct_block.get());
                    break;
                case "mem" :
                    Runtime r = Runtime.getRuntime();
                    System.out.println("Acceptor:"+Main.listener.acceptNumber.get()+"\nAddressGetter:"+Main.listener.addressGetter.get()+"\nComputeTaks:"+Main.listener.computeNumber.get()+"\nReader:"+Main.listener.readNumber.get()+"\nVersionTasks:"+Main.listener.versionNumber.get()+"\nHeader liberi: "+SerializedMessage.headerC.get()+"\nPayload liberi: "+SerializedMessage.payloadC.get()+"\nMemoria usata:"+((r.maxMemory()-r.freeMemory())/(1024*1024)));
                    break;
                case "agent" :
                    for(String str : Main.userAgents.keySet())
                    {
                        System.out.println(str+": "+Main.userAgents.get(str).get());
                    }
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
