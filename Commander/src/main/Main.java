package main;

import Client.commands.Command;
import Client.commands.Exit;
import Client.commands.Send;
import Client.commands.Update;
import Client.messages.GetAddress;
import Client.messages.GetData;
import Client.messages.Message;
import Client.messages.Ping;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Matteo on 15/11/2016.
 */
public class Main {

    public static Scanner in;
    public static List<InetAddress> peers;

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        in = new Scanner(System.in);
        String ip = in.nextLine();
        Socket skt = new Socket();
        skt.connect(new InetSocketAddress(InetAddress.getByName(ip),4201));
        System.out.println("Connesso al client");

        ObjectOutputStream out = new ObjectOutputStream(skt.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(skt.getInputStream());

        peers = (List<InetAddress>) input.readUnshared();

        while(true)
        {
            System.out.println("1:Update Peer List\n2:Send new Message\n3:Exit");
            int command = in.nextInt();
            Command c = null;
            switch (command)
            {
                case 1 :
                    c = new Update();
                    break;
                case 2 :
                    c = createMessage();
                    break;
                case 3 :
                    c = new Exit();
                    break;
                default:
            }
            if(c != null)
                out.writeUnshared(c);
            if(c instanceof Exit)
                break;
        }


    }

    private static Command createMessage() {

        Message m = null;
        while(true)
        {
            System.out.println("1:Ping\n2:getAddr\n3:Inv\n4:GetData");
            int type = in.nextInt();
            if(m == null)
                switch (type)
                {
                    case 1:
                        m = createPing();
                        break;
                    case 2:
                        m = createGetAddr();
                        break;
                    case 3:
                        m = createInv();
                        break;
                    case 4:
                        m = createGetData();
                        break;
                    default:
                        break;
                }
            if(m == null)
                continue;
            System.out.println("1:Send the message to every Peer\n2:Send the message to random peers\n3:Send the message to specific peers");
            int sendTo = in.nextInt();
            List<InetAddress> addresses = null;
            switch (sendTo)
            {
                case 1:
                    addresses = peers;
                    break;
                case 2:
                    System.out.println("How many random Peers?");
                    int num = in.nextInt();
                    Random r = new Random();
                    addresses = new LinkedList<>();
                    for(int i = 0; i < num; i++)
                        addresses.add(peers.get(r.nextInt()));
                    break;
                case 3:
                    addresses = selectPeer();
                    break;
                default:
                    break;
            }
            if(addresses == null)
                continue;
            return new Send(m,addresses);
        }
    }

    private static List<InetAddress> selectPeer() {
        boolean exit = false;
        int i = 0;
        LinkedList<InetAddress> ret = new LinkedList<>();
        while(!exit)
        {
            for(int j = 0; j < 10; j++)
            {
                System.out.println(j+": "+peers.get(i));
                i++;
            }
            System.out.println("10:Next...");
            int choice = in.nextInt();
            if(choice < 10)
            {
                ret.add(peers.get(i - 10 + choice));
                while(true)
                {
                    System.out.println("Continue? Y/N");
                    String c = in.nextLine();
                    if(c.toLowerCase().equals("n"))
                    {
                        exit = true;
                        break;
                    }
                    if(c.toLowerCase().equals("y"))
                        break;
                }
            }
            if(choice == 10)
                continue;
            if(choice > 10)
                i-=10;
        }

        return ret;
    }

    private static Message createGetData() {
        return new GetData();
    }

    private static Message createInv() {
        //TODO:
        return null;
    }

    private static Message createGetAddr() {
        return new GetAddress();
    }

    private static Message createPing() {
        Ping m = new Ping();
        m.setNonce(new Random().nextLong());
        return m;
    }

}
