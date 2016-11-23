package Client.commands;

import Client.Main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by machiara on 22/11/16.
 */
public class Show extends Command {

    private InetAddress yourAddr;
    private InetAddress myAddr;

    public Show(InetAddress yourAddr,InetAddress myAddr){
        this.yourAddr = yourAddr;
        this.myAddr = myAddr;
    }

    @Override
    public void execute(ObjectOutputStream out) {
        Main.followed = Main.peers.get(yourAddr.getHostAddress());
        try {
            out.writeUnshared(new NullResponse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
