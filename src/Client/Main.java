package Client;

import Client.bitio.LittleEndianOutputStream;
import Client.messages.PeerAddress;
import Client.messages.Version;
import Client.network.SocketListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Matteo on 07/10/2016.
 */
public class Main {


    public static SocketListener listener = new SocketListener();

    public static void main(String [] args) throws IOException {
        Version v = new Version();
        PeerAddress my = new PeerAddress();
        PeerAddress your = new PeerAddress();
        my.setAddress(InetAddress.getByAddress(new byte []{0x0A, 0x00, 0x00, 0x01}));
        your.setAddress(InetAddress.getByAddress(new byte [] {0x0A, 0x00, 0x00, 0x02}));
        my.setPort(8333);
        your.setPort(8333);

        v.setMyAddress(my);
        v.setYourAddress(your);
        v.setHeight(212672);
        v.setTimestamp(System.currentTimeMillis());
        v.setNonce(1);
        v.setVersion(31900);
        v.setUserAgent("/Satoshi:0.7.2/");
        v.setServices(1);
        v.setRelay(false);

        ByteArrayOutputStream out = new ByteArrayOutputStream(100);
        LittleEndianOutputStream s = new LittleEndianOutputStream(out);
        v.write(s);
        byte [] b = out.toByteArray();
        for(int i = 0; i < b.length; i++)
        {
            System.out.print(String.format("%02X ", b[i]));
            if(i == 3 || i == 11 || i == 19 || i == 45 || i == 71 || i == 79 || i == 95)
                System.out.print("\n");
        }

    }


}
