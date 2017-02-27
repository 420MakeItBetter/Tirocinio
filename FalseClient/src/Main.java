import Client.BitConstants;
import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;
import Client.messages.*;
import Client.utils.IOUtils;
import io.nayuki.bitcoin.crypto.Sha256;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Matteo on 13/12/2016.
 */
public class Main {

    public static int others;
    public static int mine;
    public static LinkedBlockingQueue<Inventory> invs = new LinkedBlockingQueue<>();
    public static List<PeerAddress> addr = new LinkedList<>();

    public static AtomicLong nonce = new AtomicLong(-1);
    public static AtomicBoolean sent = new AtomicBoolean(false);

    static Socket skt = null;
    static LittleEndianInputStream leis = null;
    static LittleEndianOutputStream leos = null;
    static ByteArrayOutputStream outarray = null;
    static OutputStream out = null;
    static Thread snd = null;
    static boolean flag;

    public static void initialize() throws IOException {
        InetAddress addr = InetAddress.getByName("192.81.132.82");
        skt = new Socket();
        skt.connect(new InetSocketAddress(addr,8333));

        Version v = new Version();
        PeerAddress my = new PeerAddress();
        my.setAddress(InetAddress.getLocalHost());
        my.setPort(BitConstants.PORT);
        my.setService(0);
        PeerAddress your = new PeerAddress();
        your.setAddress(InetAddress.getByName("192.81.132.82"));
        your.setPort(8333);
        your.setService(0);
        v.setMyAddress(my);
        v.setYourAddress(your);
        v.setServices(1);
        v.setTimestamp(System.currentTimeMillis() / BitConstants.TIME);
        v.setNonce(1000001);
        v.setVersion(BitConstants.VERSION);
        v.setUserAgent("/TestClient.0.0.1/");
        v.setHeight(BitConstants.LASTBLOCK);
        v.setRelay(true);

        out = skt.getOutputStream();
        leis = new LittleEndianInputStream(skt.getInputStream());


        outarray = new ByteArrayOutputStream();
        leos = new LittleEndianOutputStream(outarray);

        v.write(leos);
        out.write(BitConstants.MAGIC);
        out.write("version".getBytes());
        out.write(new byte [] {0,0,0,0,0});
        out.write(IOUtils.intToByteArray(v.getLength()));
        System.out.println(v.getLength());
        out.write(IOUtils.getChecksum(Sha256.getDoubleHash(outarray.toByteArray()).toBytes()));
        out.write(outarray.toByteArray());

        System.out.println("Inviato Version");
        outarray.reset();
        sent.set(false);

        snd =new Thread(new Sender(leos,outarray,out));
        snd.start();
        flag = true;
        System.out.println("initialize");
    }

    public static void main(String [] args) throws IOException {

        new Thread(new StatWriter()).start();





        byte [] header = new byte [24];
        while(true)
        {
            initialize();
            while (flag)
            {

                int i = 0;
                while (i < 24)
                {
                    i += leis.read(header, i, 24 - i);
                }
                String s = new String(header, 4, 12).trim();
                System.out.println(s);
                switch (s)
                {
                    case "verack":
                        VerAck vva = new VerAck();
                        vva.read(leis);
                        break;
                    case "version":
                        Version vv = new Version();
                        vv.read(leis);
                        VerAck va = new VerAck();
                        out.write(BitConstants.MAGIC);
                        out.write("verack".getBytes());
                        out.write(new byte[]{0, 0, 0, 0, 0, 0});
                        out.write(IOUtils.intToByteArray(va.getLength()));
                        out.write(IOUtils.intToByteArray(BitConstants.CHECKSUM));
                        va.write(leos);
                        out.write(outarray.toByteArray());
                        outarray.reset();
                        break;
                    case "addr":
                        Address a = new Address();
                        byte[] buffer = new byte[((header[19] & 0xFF) << 24 | (header[18] & 0xFF) << 16 | (header[17] & 0xFF) << 8 | (header[16] & 0xFF))];
                        int r = 0;
                        while (r < buffer.length)
                            r += leis.read(buffer, r, buffer.length - r);
                        LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(buffer));
                        a.read(in);
                        in.close();
                        addrStats(a);
                        if(sent.get())
                            flag = false;
                        break;
                    case "ping":
                        Ping pi = new Ping();
                        pi.read(leis);
                        nonce.set(pi.getNonce());
                        break;
                    case "inv":
                        Inventory inv = new Inventory();
                        byte[] bufferr = new byte[((header[19] & 0xFF) << 24 | (header[18] & 0xFF) << 16 | (header[17] & 0xFF) << 8 | (header[16] & 0xFF))];
                        int rr = 0;
                        while (rr < bufferr.length)
                            rr += leis.read(bufferr, rr, bufferr.length - rr);
                        LittleEndianInputStream inn = new LittleEndianInputStream(new ByteArrayInputStream(bufferr));
                        inv.read(inn);
                        inv.setLength(bufferr.length);
                        try
                        {
                            invs.put(inv);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        inn.close();
                        break;
                    default:
                        int size = ((header[19] & 0xFF) << 24 | (header[18] & 0xFF) << 16 | (header[17] & 0xFF) << 8 | (header[16] & 0xFF));
                        System.out.println(size);
                        if (size > 0)
                            for (int read = 0; read < size; read++)
                                leis.read();
                }
            }

            destroy();

        }
    }

    private static void destroy() {
        try
        {
            skt.close();
            leos.close();
            leis.close();
            outarray.close();
            snd.interrupt();
            System.out.println("Destroy");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private static void addrStats(Address a) {

        for(PeerAddress pa : a.getAddresses())
        {
            if(pa.getAddress().getHostAddress().contains("131.114.8") && !pa.getAddress().getHostAddress().equals("131.114.88.218"))
            {
                addr.add(pa);
                mine++;
            }
            else
                others++;
        }
    }



    private static class StatWriter implements Runnable {


        @Override
        public void run() {
            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(new File("stat"));
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
                try
                {
                    out.write(("\nMiei Indirizzi: "+mine+"\nAltri Indirizzi: "+others).getBytes());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }


    private static class Sender implements Runnable {

        LittleEndianOutputStream leos;
        ByteArrayOutputStream outarray;
        OutputStream out;

        public Sender(LittleEndianOutputStream leos, ByteArrayOutputStream outarray, OutputStream out) {
            this.leos = leos;
            this.outarray = outarray;
            this.out = out;
            System.out.println("creato");
        }

        @Override
        public void run() {

            while(!Thread.currentThread().isInterrupted())
            {
                System.out.println("Dormo");
                try
                {
                    Thread.currentThread().sleep(1000*60);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    break;
                }
                if(Thread.currentThread().isInterrupted())
                    break;
                System.out.println("Mi Sveglio");
                if(nonce.get() != -1)
                {
                    try
                    {
                        Pong po = new Pong();
                        po.setNonce(nonce.get());
                        po.write(leos);
                        out.write(BitConstants.MAGIC);
                        out.write("pong".getBytes());
                        out.write(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
                        out.write(IOUtils.intToByteArray(po.getLength()));
                        out.write(IOUtils.getChecksum(Sha256.getDoubleHash(outarray.toByteArray()).toBytes()));
                        out.write(outarray.toByteArray());
                        System.out.println("Sent pong");
                        outarray.reset();
                    }
                    catch (IOException e)
                    {}
                    nonce.set(-1);
                }
                Inventory inv = null;
                try
                {
                    inv = invs.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                while(inv != null)
                {
                    try {
                        inv.write(leos);
                        out.write(BitConstants.MAGIC);
                        out.write("inv".getBytes());
                        out.write(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
                        out.write(IOUtils.intToByteArray(inv.getLength()));
                        out.write(IOUtils.getChecksum(Sha256.getDoubleHash(outarray.toByteArray()).toBytes()));
                        out.write(outarray.toByteArray());
                        System.out.println("Sent Inv");
                        outarray.reset();
                    }catch (IOException e)
                    {}
                    try
                    {
                        inv = invs.poll(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                GetAddress ga = new GetAddress();
                try
                {
                    out.write(BitConstants.MAGIC);
                    out.write("getaddr".getBytes());
                    out.write(new byte[]{0, 0, 0, 0, 0});
                    out.write(IOUtils.intToByteArray(ga.getLength()));
                    out.write(IOUtils.intToByteArray(BitConstants.CHECKSUM));
                    //ga.write(leos);
                    //out.write(outarray.toByteArray());
                    //outarray.reset();
                    System.out.println("Sent getaddress");
                    sent.set(true);
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }
}
