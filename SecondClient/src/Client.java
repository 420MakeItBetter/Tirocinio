import Client.bitio.LittleEndianInputStream;
import Client.bitio.LittleEndianOutputStream;
import Client.messages.*;
import Client.BitConstants;
import Client.network.Peer;
import Client.utils.IOUtils;
import io.nayuki.bitcoin.crypto.Sha256;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

/**
 * Created by machiara on 27/02/17.
 */
public class Client implements Runnable {

    Random r;
    Socket skt;

    public Client(Socket skt) {
        this.skt = skt;
        r = new Random();
    }

    @Override
    public void run() {
        try
        {
            Version v = new Version();
            PeerAddress my = new PeerAddress();
            my.setAddress(InetAddress.getLocalHost());
            my.setPort(BitConstants.PORT);
            my.setService(0);
            PeerAddress your = new PeerAddress();
            your.setAddress(skt.getInetAddress());
            your.setPort(8333);
            your.setService(0);
            v.setMyAddress(my);
            v.setYourAddress(your);
            v.setServices(0);
            v.setTimestamp(System.currentTimeMillis() / BitConstants.TIME);
            v.setNonce(1000001);
            v.setVersion(BitConstants.VERSION);
            v.setUserAgent("/TestClient.0.0.1/");
            v.setHeight(BitConstants.LASTBLOCK);
            v.setRelay(true);

            OutputStream out = skt.getOutputStream();

            ByteArrayOutputStream outarray = new ByteArrayOutputStream();
            LittleEndianOutputStream leos = new LittleEndianOutputStream(outarray);

            LittleEndianInputStream leis = new LittleEndianInputStream(skt.getInputStream());
            v.write(leos);

            out.write(BitConstants.MAGIC);
            out.write("version".getBytes());
            out.write(new byte [] {0,0,0,0,0});
            out.write(IOUtils.intToByteArray(v.getLength()));
            System.out.println(v.getLength());
            out.write(IOUtils.getChecksum(Sha256.getDoubleHash(outarray.toByteArray()).toBytes()));
            out.write(outarray.toByteArray());

            outarray.reset();

            byte [] buffer = new byte [24];
            while(true)
            {
                int i = 0;
                while(i < 24)
                {
                    i+= leis.read(buffer,i,24 - i);
                }
                String s = new String(buffer, 4, 12).trim();
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
                        byte[] msg = new byte[((buffer[19] & 0xFF) << 24 | (buffer[18] & 0xFF) << 16 | (buffer[17] & 0xFF) << 8 | (buffer[16] & 0xFF))];
                        int r = 0;
                        while (r < msg.length)
                            r += leis.read(msg, r, msg.length - r);
                        LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(msg));
                        a.read(in);
                        in.close();
                        addrStats(a);
                        break;
                    case "ping":
                        Ping pi = new Ping();
                        pi.read(leis);
                        Pong p = new Pong();
                        p.setNonce(pi.getNonce());
                        out.write(BitConstants.MAGIC);
                        out.write("pong".getBytes());
                        out.write(new byte[]{0,0,0,0,0,0,0,0});
                        out.write(IOUtils.intToByteArray(p.getLength()));
                        p.write(leos);
                        out.write(IOUtils.getChecksum(Sha256.getDoubleHash(outarray.toByteArray()).toBytes()));
                        out.write(outarray.toByteArray());
                        outarray.reset();
                        break;
                    case "inv":
                        Inventory inv = new Inventory();
                        byte[] bufferr = new byte[((buffer[19] & 0xFF) << 24 | (buffer[18] & 0xFF) << 16 | (buffer[17] & 0xFF) << 8 | (buffer[16] & 0xFF))];
                        int rr = 0;
                        while (rr < bufferr.length)
                            rr += leis.read(bufferr, rr, bufferr.length - rr);
                        if(this.r.nextBoolean())
                        {
                            out.write(buffer);
                            out.write(bufferr);
                        }
                        break;
                    default:
                        int size = ((buffer[19] & 0xFF) << 24 | (buffer[18] & 0xFF) << 16 | (buffer[17] & 0xFF) << 8 | (buffer[16] & 0xFF));
                        System.out.println(size);
                        if (size > 0)
                            for (int read = 0; read < size; read++)
                                leis.read();
                }

            }

        }catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                skt.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }

    }

    private void addrStats(Address a) {

        for(PeerAddress p : a.getAddresses())
        {
            if(p.getAddress().getHostAddress().contains("131.114.8") && !p.getAddress().getHostAddress().equals("131.114.88.218"))
                Main.mine.incrementAndGet();
            else
                Main.other.incrementAndGet();
        }
        System.out.println("mine: "+Main.mine.get()+" others: "+Main.other.get());
    }
}
