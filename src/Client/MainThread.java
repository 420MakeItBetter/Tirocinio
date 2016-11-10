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

        File f = new File("UserAgentStats");
        if(!f.exists())
            try
            {
                f.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        FileOutputStream out = null;
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                Thread.sleep(1000*30);
                out = new FileOutputStream(f);
                HashMap<String,Integer> map = new HashMap<>();
                for(String str : Main.userAgents.keySet())
                {
                    out.write(str.getBytes());
                    out.write(": ".getBytes());
                    out.write(String.valueOf(Main.userAgents.get(str).get()).getBytes());
                    out.write('\n');
                    char [] arr = new char [500];
                    int i = 0;
                    for(char c : str.toCharArray())
                    {
                        if(c == ':')
                            break;
                        else
                        {
                            if(c == '/')
                                continue;
                            if(i < 500)
                            {
                                arr[i] = c;
                                i++;
                            }
                            else
                                break;
                        }
                    }
                    Integer val = null;
                    if(map.containsKey(String.valueOf(arr).trim()))
                        val = map.get(String.valueOf(arr).trim());
                    else
                        val = 0;
                    val+=Main.userAgents.get(str).get();
                    map.put(String.valueOf(arr).trim(),val);
                }
                out.write("------------------------\n".getBytes());
                for(String s : map.keySet())
                {
                    out.write(s.getBytes());
                    out.write(": ".getBytes());
                    out.write(String.valueOf(map.get(s)).getBytes());
                    out.write('\n');
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }
}
