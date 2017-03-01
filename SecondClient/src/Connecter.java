import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by machiara on 27/02/17.
 */
public class Connecter implements Runnable{

    private BlockingQueue<String> address;


    Connecter(){
        address = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {

        while (true)
        {
                String s = null;
            try
            {
                s = address.take();
                InetAddress addr = InetAddress.getByName(s);
                Socket skt = new Socket();
                skt.connect(new InetSocketAddress(addr,8333),10000);
                new Thread(new Client(skt)).start();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            } catch (UnknownHostException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                if(s != null)
                    address.add(s);
            }
        }


    }

    public void addAddress(String addr){
        address.add(addr);
    }
}
