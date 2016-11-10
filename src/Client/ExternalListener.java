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
            while(!Thread.currentThread().isInterrupted())
            {
                boolean exit = false;
                Socket skt = server.accept();
                try
                {
                    InputStream in = skt.getInputStream();
                    OutputStream out = skt.getOutputStream();
                    StringBuilder builder = new StringBuilder();
                    String command;
                    while (!exit)
                    {
                        int b;
                        int i = 0;
                        byte[] buf = new byte[100];
                        while ((b = in.read()) != '\0')
                        {
                            if(b == -1)
                            {
                                exit = true;
                                break;
                            }
                            buf[i] = (byte) b;
                            i++;
                        }
                        command = new String(buf).trim();
                        System.out.println(command);
                        switch (command)
                        {
                            case "exit":
                                exit = true;
                                break;
                            case "stat":
                                int open = 0;
                                int handshake = 0;
                                int close = 0;
                                for (Peer p : Main.peers.values())
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
                                builder.append("Connessioni aperte: ")
                                        .append(open)
                                        .append("\nConnessioni in fase di handshake: ")
                                        .append(handshake)
                                        .append("\nConnessioni chiuse: ")
                                        .append(close)
                                        .append("\nConnessioni totali:")
                                        .append(open + handshake + close)
                                        .append("\nConnessioni richieste in entrata:")
                                        .append(Main.listener.connected.get());
                                break;
                            case "inv":
                                builder.append("Errors: ")
                                        .append(Main.invStat.error.get())
                                        .append("\nTransactions: ")
                                        .append(Main.invStat.transiction.get())
                                        .append("\nBlocks: ")
                                        .append(Main.invStat.block.get())
                                        .append("\nFiltered Block: ")
                                        .append(Main.invStat.filtered_block.get())
                                        .append("\nCMPCT Blocks: ")
                                        .append(Main.invStat.cmpct_block.get());
                                break;
                            case "mem":
                                Runtime r = Runtime.getRuntime();
                                builder.append("Acceptor:")
                                        .append(Main.listener.acceptNumber.get())
                                        .append("\nAddressGetter:" + Main.listener.addressGetter.get())
                                        .append("\nComputeTaks:")
                                        .append(Main.listener.computeNumber.get())
                                        .append("\nReader:" + Main.listener.readNumber.get())
                                        .append("\nVersionTasks:" + Main.listener.versionNumber.get())
                                        .append("\nHeader liberi: ")
                                        .append(SerializedMessage.headerC.get())
                                        .append("\nPayload liberi: ")
                                        .append(SerializedMessage.payloadC.get())
                                        .append("\nMemoria usata:")
                                        .append(((r.maxMemory() - r.freeMemory()) / (1024 * 1024)));
                                break;
                            case "agent":
                                for (String str : Main.userAgents.keySet())
                                {
                                    builder.append(str)
                                            .append(": ")
                                            .append(Main.userAgents.get(str).get())
                                            .append("\n");
                                }
                                break;
                            case "uptime":
                                long time = System.currentTimeMillis() - start;
                                builder.append("Client in esecuzione da: \n")
                                        .append(time / (1000 * 60 * 60 * 24))
                                        .append("G ")
                                        .append((time % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
                                        .append("H ")
                                        .append((time % (1000 * 60 * 60)) / (1000 * 60))
                                        .append("m ")
                                        .append(((time % (1000 * 60)) / 1000))
                                        .append("s");
                                break;
                            default:
                                break;

                        }
                        builder.append('\0');
                        System.out.println(builder.toString());
                        out.write(builder.toString().getBytes());
                        builder = new StringBuilder();
                    }
                }
                catch(IOException e)
                {
                    try
                    {
                        skt.close();
                    }catch (IOException e1)
                    {}
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
