package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by Matteo on 03/11/2016.
 */
public class Client {


    public static void main(String [] args) throws IOException {
        Scanner in = new Scanner(System.in);

        InetAddress addr = InetAddress.getByName(in.nextLine());

        Socket skt = new Socket();

        skt.connect(new InetSocketAddress(addr,4200));
        System.out.println("Connesso");
        OutputStream out = skt.getOutputStream();
        InputStream inn = skt.getInputStream();

        while(true)
        {
            String command = in.nextLine();
            out.write(command.getBytes());
            out.write('\0');
            int b;
            while((b = inn.read()) != '\0')
                System.out.print((char)b);
            System.out.println();
        }

    }

}
