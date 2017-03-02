import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by machiara on 27/02/17.
 */
public class ServerClient implements Runnable {

    @Override
    public void run() {
        try
        {
            ServerSocket server = new ServerSocket(8333);
            while(true)
            {
                Socket skt = server.accept();
                System.out.println("Connessione in entrata");
                new Thread(new Client(skt)).start();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
