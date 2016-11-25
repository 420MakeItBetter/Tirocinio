package main;

import Client.*;
import Client.commands.Exit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by machiara on 22/11/16.
 */
public class MainGui extends JFrame implements Runnable, WindowListener {

    public MainGui(Socket s,Socket s1){
        setSize(1000,1000);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        PeerLister right = new PeerLister();
        MessageLister center = new MessageLister(s,s1);

        setLayout(new BorderLayout());
        addWindowListener(this);
        add(right,BorderLayout.LINE_END);
        add(center,BorderLayout.CENTER);
    }


    @Override
    public void run() {
        SwingUtilities.invokeLater(() ->{
            Main.window.dispose();
            Main.window = this;
        });
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            Main.out.writeUnshared(new Exit());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
