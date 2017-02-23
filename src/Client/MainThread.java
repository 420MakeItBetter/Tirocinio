package Client;

import Client.messages.SerializedMessage;
import Client.network.Peer;
import Client.network.PeerState;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by machiara on 20/10/16.
 *
 */
public class MainThread implements Runnable {


    @Override
    public void run() {

        long start = System.currentTimeMillis();
        File f = new File("UserAgentStats");
        File stat = new File("Statistics");
        if(!f.exists())
            try
            {
                f.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        if(!stat.exists())
            try
            {
                stat.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        FileOutputStream out1 = null;
        FileOutputStream out2 = null;
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                Thread.sleep(1000*60*30);
                out1 = new FileOutputStream(f);
                out2 = new FileOutputStream(stat);
                HashMap<String,Integer> map = new HashMap<>();
                HashMap<String, Integer> m = new HashMap<>();
                for(Peer p : Main.peers.values())
                {
                    if(p.getAgent() == null)
                        continue;
                    if (!map.containsKey(p.getAgent()))
                        map.put(p.getAgent(), 1);
                    else
                        map.put(p.getAgent(), 1 + map.get(p.getAgent()));
                }

                for(String str : map.keySet())
                {
                    if(str == null)
                        continue;
                    out1.write(str.getBytes());
                    out1.write(": ".getBytes());
                    out1.write(String.valueOf(map.get(str)).getBytes());
                    out1.write('\n');
                    char[] arr = new char[500];
                    int i = 0;
                    for (char c : str.toCharArray())
                    {
                        if (c == ':')
                            break;
                        else
                        {
                            if (c == '/')
                                continue;
                            if (i < 500)
                            {
                                arr[i] = c;
                                i++;
                            } else
                                break;
                        }
                    }
                    Integer val = null;
                    if (m.containsKey(String.valueOf(arr).trim()))
                        val = map.get(String.valueOf(arr).trim());
                    else
                        val = 0;
                    val += map.get(str);
                    m.put(String.valueOf(arr).trim(), val);
                }
                out1.write("------------------------\n".getBytes());
                for(String s : m.keySet())
                {
                    out1.write(s.getBytes());
                    out1.write(": ".getBytes());
                    out1.write(String.valueOf(m.get(s)).getBytes());
                    out1.write('\n');
                }

                map = null;
                m = null;
                out1.close();
                StringBuilder builder = new StringBuilder();
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
                        .append(Main.listener.connected.get())
                        .append("\n\n");
                builder.append("Errors: ")
                        .append(Main.invStat.error.get())
                        .append("\nTransactions: ")
                        .append(Main.invStat.transiction.get())
                        .append("\nBlocks: ")
                        .append(Main.invStat.block.get())
                        .append("\nFiltered Block: ")
                        .append(Main.invStat.filtered_block.get())
                        .append("\nCMPCT Blocks: ")
                        .append(Main.invStat.cmpct_block.get())
                        .append("\n\n");
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
                out2.write(builder.toString().getBytes());
                out2.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }

    }
}
