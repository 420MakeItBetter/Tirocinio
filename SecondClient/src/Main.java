import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by machiara on 27/02/17.
 */
public class Main {



    public static AtomicInteger mine = new AtomicInteger(0);
    public static AtomicInteger other = new AtomicInteger(0);

    public static void main(String [] args){

        ServerClient serverClient = new ServerClient();
        new Thread(serverClient).start();
        Connecter connecter = new Connecter();
        new Thread(connecter).start();
        InetAddress addr = null;
        try
        {
            addr = InetAddress.getByName("131.114.88.218");
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        Socket skt = new Socket();
        try
        {
            skt.connect(new InetSocketAddress(addr,4201));
            System.out.println("Connected");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            System.out.println("Creato reader");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            try
            {
                String s = reader.readLine();
                System.out.println("received"+s);
                connecter.addAddress(s);
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }


    }

}
