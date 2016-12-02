import Client.bitio.LittleEndianInputStream;
import Client.commands.Send;
import Client.commands.Update;
import Client.messages.Address;
import Client.messages.GetAddress;
import Client.messages.PeerAddress;
import Client.network.Peer;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Matteo on 28/11/2016.
 */
public class Main {

    public static Socket mainSkt = new Socket();
    public static Socket msgSkt = new Socket();
    public static Socket addrSkt = new Socket();
    public static NavigableSet<DirtyString> set = new TreeSet<>();
    public static Thread mainThread;
    public static AtomicBoolean abort = new AtomicBoolean(false);
    public static List<PeerAddress> addresses = new LinkedList<>();
    public static Set<PeerAddress> addressesSet = new HashSet<>();
    public static FileOutputStream out = null;
    public static AtomicBoolean done = new AtomicBoolean(false);

    public static void main(String [] args) throws IOException {
        for(int i = 83; i < 88; i++)
        {
            if(addresses.size() == 1000)
                break;
            if(84 <= i && i <= 85)
                continue;
            for(int j = 0; j <= 255; j++)
            {
                if(addresses.size() == 1000)
                    break;
                System.out.println("ok");
                if(i == 88 && j == 218)
                    continue;
                PeerAddress addr = new PeerAddress();
                addr.setService(1);
                addr.setTime((int) (System.currentTimeMillis() / 1000) - 60*10);
                addr.setPort(8333);
                addr.setAddress(InetAddress.getByName("131.114."+String.valueOf(i)+"."+String.valueOf(j)));
                addresses.add(addr);
                System.out.println(addr.getAddress().getHostAddress());
            }
        }
        addressesSet.addAll(addresses);
        File f = new File("Matches");
        if(!f.exists())
            try
            {
                f.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        try
        {
            out = new FileOutputStream(f);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        mainSkt.connect(new InetSocketAddress(InetAddress.getByName("131.114.88.218"), 4201));
        msgSkt.connect(new InetSocketAddress(InetAddress.getByName("131.114.88.218"), 5000));
        addrSkt.connect(new InetSocketAddress(InetAddress.getByName("131.114.88.218"), 5001));

        mainThread = new Thread(new MainThread());
        mainThread.start();
        new Thread(new MessageThread()).start();
        new Thread(new AddrThread()).start();

    }

    private static class MainThread implements Runnable {


        @Override
        public void run() {
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            String chose = null;
            try
            {
                in = new ObjectInputStream(mainSkt.getInputStream());
                out = new ObjectOutputStream(mainSkt.getOutputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            List<InetAddress> addr = new LinkedList<>();
            while(true)
            {
                try
                {
                    out.writeUnshared(new Update());
                    List<String> peers = (List<String>) in.readUnshared();
                    for(String s : peers)
                        if(s.contains("176.10.116.242"))
                        {
                            chose = s;
                            System.out.println("Trovato Nodo");
                            break;
                        }

                    for(int i = 0; i < 500; i++)
                        if(i < peers.size())
                        {
                            String s = peers.get(i);
                            if(!s.contains("176.10.116.242"))
                                addr.add(InetAddress.getByName(s.split("/")[0]));
                        }
                    if(chose != null)
                        break;
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            while(true)
            {
                Address m = new Address();
                for(PeerAddress a : addresses)
                {
                    a.setTime((int) (System.currentTimeMillis() / 1000) - 60*10);
                }
                m.getAddresses().addAll(addresses);
                List<InetAddress> tmp = new LinkedList<>();
                try
                {
                    tmp.add(InetAddress.getByName(chose.split("/")[0]));
                } catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    out.writeUnshared(new Send(m,tmp));
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    System.out.println("Invio getAddr");
                        System.out.println("Pronto ad inviare il primo");
                    for(int i = 0; i < 1000; i++)
                    {
                        out.writeUnshared(new Send(new GetAddress(), addr));

                        System.out.println("Invio n "+i);
                        try
                        {
                            Thread.currentThread().sleep(1000);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Inviati");
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    Thread.currentThread().sleep(1000*60);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }


            }

        }
    }

    private static class DirtyString implements Comparable<DirtyString>{
        public String str;
        public boolean dirty;

        @Override
        public boolean equals(Object obj) {
            return str.equals(((DirtyString) obj).str);
        }

        @Override
        public int compareTo(DirtyString o) {
            return str.compareTo(o.str);
        }
    }

    private static class MessageThread implements Runnable {
        @Override
        public void run() {
            ObjectInputStream in = null;
            try
            {
                in = new ObjectInputStream(msgSkt.getInputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            while(!abort.get())
            {
                try
                {
                    String s = (String) in.readUnshared();
                    Main.out.write((s+"\n").getBytes());
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class AddrThread implements Runnable {

        @Override
        public void run() {

            InputStream in = null;
            try
            {
                in = addrSkt.getInputStream();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            byte [] buffer = new byte [24];
            byte [] msg = new byte [50000];
            byte [] peer = new byte [100];
            int size = 0;
            while(!abort.get())
            {
                try
                {

                    in.read(buffer);
                    size = ((buffer[19] & 0xFF) << 24 | (buffer[18] & 0xFF) << 16 | (buffer[17] & 0xFF) << 8 | (buffer[16] & 0xFF));
                    if(size > 0)
                    {
                        int read = 0;
                        while(read < size)
                         read += in.read(msg,read,size - read);
                    }
                    int s = in.read();
                    in.read(peer,0,s);
                    if(msg == null)
                        continue;
                    LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(msg));
                    Address addr = new Address();
                    addr.read(leis);
                    for(PeerAddress pa : addr.getAddresses())
                    {
                        if(pa.getAddress().getHostAddress().contains("131.114."))
                            if(!pa.getAddress().getHostAddress().equals("131.114.88.218"))
                                out.write(("Address received: "+pa.getAddress().getHostAddress()+" from "+new String(peer)+"\n").getBytes());
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
