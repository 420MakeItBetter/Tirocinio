import Client.network.Peer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Matteo on 15/12/2016.
 */
public class Observer {

    public static HashSet<String> peers = new HashSet<>();

    public static void main(String [] args){

        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(new File("ConnectedStat"));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        while(true)
        {
            try
            {
                Thread.currentThread().sleep(1000*60);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            int newPeers = 0;
            Document doc = null;
            try
            {
                doc = Jsoup.connect("http://176.10.116.242/xbt_cgi/node_status.pl").get();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            Elements div = doc.getElementsByTag("div");

            int i = 0;
            for (Element el : div)
            {
                if (i % 60 == 0)
                {
                    String s = el.text().split("")[0];
                    if(!peers.contains(s))
                    {
                        peers.add(s);
                        newPeers++;
                    }
                }
                i++;
            }
            try
            {
                out.write(("Nuovi Indirizzi: "+newPeers).getBytes());
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }

    }

}
