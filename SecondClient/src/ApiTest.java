import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by machiara on 09/03/17.
 */
public class ApiTest {

    public static void main(String [] args) throws IOException {

        Socket skt = new Socket();
        skt.connect(new InetSocketAddress(InetAddress.getByName("131.114.88.218"),1994));

        System.out.println("connesso");
        DataOutputStream out = new DataOutputStream(skt.getOutputStream());
        DataInputStream in = new DataInputStream(skt.getInputStream());

        new Thread(new Reader(in)).start();

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(array);
        stream.writeInt(1);
        stream.writeLong(1);
        stream.writeInt(1);
        stream.write("addr".getBytes());
        stream.write(new byte [] {0,0,0,0,0,0,0,0});
        stream.write(1);

        out.writeInt(array.size());
        out.write(array.toByteArray());

    }

    private static class Reader implements Runnable {

        DataInputStream in;

        public Reader(DataInputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            int l;
            try
            {
                while (true)
                {
                    l = in.readInt();
                    int type = in.readInt();
                    switch (type)
                    {
                        case 1 :
                            System.out.println("ack");
                            if(in.readInt() == 0)
                            {
                                System.out.println(in.readLong());
                            }
                            break;
                        case 2 :
                            System.out.println("msg received");
                            long id = in.readLong();
                            byte [] ip = new byte [16];
                            in.read(ip);
                            InetAddress addr = InetAddress.getByAddress(ip);
                            System.out.println("from "+addr.getHostAddress());
                            byte [] header = new byte [24];
                            in.read(header);
                            break;
                    }
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
