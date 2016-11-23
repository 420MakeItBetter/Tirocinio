package main;

import javax.swing.*;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by machiara on 22/11/16.
 */
public class RefreshTask extends ResponseTask{

    List<String> peers;

    @Override
    void setResponse(Object o) {
        peers = (List<String>) o;
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            PeerLister.peersModel.clear();
            for(String s : peers)
                PeerLister.peersModel.addElement(s);
        });
    }
}
