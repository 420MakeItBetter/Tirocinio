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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Matteo on 13/12/2016.
 */
public class Main {

    public static int others;
    public static int mine;
    public static List<PeerAddress> addr = new LinkedList<>();

    public static AtomicLong nonce = new AtomicLong(-1);

    public static void main(String [] args) throws IOException {

        InetAddress addr = InetAddress.getByName("176.10.116.242");
        Socket skt = new Socket();

        new Thread(new StatWriter()).start();
        skt.connect(new InetSocketAddress(addr,8333));

        Version v = new Version();
        PeerAddress my = new PeerAddress();
        my.setAddress(InetAddress.getLocalHost());
        my.setPort(BitConstants.PORT);
        my.setService(0);
        PeerAddress your = new PeerAddress();
        your.setAddress(InetAddress.getByName("176.10.116.242"));
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

        OutputStream out = skt.getOutputStream();
        LittleEndianInputStream  leis = new LittleEndianInputStream(skt.getInputStream());


        ByteArrayOutputStream outarray = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(outarray);


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

        new Thread(new Sender(leos,outarray,out)).start();

        byte [] header = new byte [24];
        while(true)
        {

            int i = 0;
            while(i < 24)
            {
                i += leis.read(header, i, 24 - i);
            }
            String s = new String(header,4,12).trim();
            System.out.println(s);
            switch (s)
            {
                case "verack" :
                    VerAck vva = new VerAck();
                    vva.read(leis);
                    break;
                case "version" :
                    Version vv = new Version();
                    vv.read(leis);
                    VerAck va = new VerAck();
                    out.write(BitConstants.MAGIC);
                    out.write("verack".getBytes());
                    out.write(new byte []{0,0,0,0,0,0});
                    out.write(IOUtils.intToByteArray(va.getLength()));
                    out.write(IOUtils.intToByteArray(BitConstants.CHECKSUM));
                    va.write(leos);
                    out.write(outarray.toByteArray());
                    outarray.reset();
                    break;
                case "addr" :
                    Address a = new Address();
                    byte [ ] buffer = new byte [((header[19] & 0xFF) << 24 | (header[18] & 0xFF) << 16 | (header[17] & 0xFF) << 8 | (header[16] & 0xFF))];
                    int r = 0;
                    while(r < buffer.length)
                        r+= leis.read(buffer,r,buffer.length - r);
                    LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(buffer));
                    a.read(in);
                    in.close();
                    addrStats(a);
                    break;
                case "ping" :
                    Ping pi = new Ping();
                    pi.read(leis);
                    nonce.set(pi.getNonce());
                    break;
                default:
                    int size = ((header[19] & 0xFF) << 24 | (header[18] & 0xFF) << 16 | (header[17] & 0xFF) << 8 | (header[16] & 0xFF));
                    System.out.println(size);
                    if(size > 0)
                        for(int read = 0; read < size; read++)
                            leis.read();
            }
        }
    }

    private static void addrStats(Address a) {

        for(PeerAddress pa : a.getAddresses())
        {
            if(pa.getAddress().getHostAddress().contains("131.114.8"))
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
        }

        @Override
        public void run() {

            while(true)
            {
                try
                {
                    Thread.currentThread().sleep(1000*60);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
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
                        outarray.reset();
                    }
                    catch (IOException e)
                    {}
                    nonce.set(-1);
                }
                GetAddress ga = new GetAddress();
                try
                {
                    out.write(BitConstants.MAGIC);
                    out.write("getaddr".getBytes());
                    out.write(new byte[]{0, 0, 0, 0, 0});
                    out.write(IOUtils.intToByteArray(ga.getLength()));
                    out.write(IOUtils.intToByteArray(BitConstants.CHECKSUM));
                    ga.write(leos);
                    out.write(outarray.toByteArray());
                    outarray.reset();
                }catch (IOException e)
                {}
            }

        }
    }
}
