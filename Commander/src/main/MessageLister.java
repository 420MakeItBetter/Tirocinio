package main;

import Client.messages.Message;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by machiara on 22/11/16.
 */
public class MessageLister extends JPanel{

    public static Socket skt;

    public static DefaultListModel<String> messageModel;

    MessageLister(Socket s){
        messageModel = new DefaultListModel<>();
        JList<String> list = new JList<>(messageModel);
        JScrollPane panel = new JScrollPane(list);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
        add(panel);
        skt = s;
        new Thread(new Handler(s)).start();
    }

    private class Handler implements Runnable{

        Socket skt;

        Handler(Socket s){
            skt = s;
        }

        @Override
        public void run() {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(skt.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true)
            {
                try {
                    String s = (String) in.readUnshared();
                    SwingUtilities.invokeLater(new AddMes(s));
                } catch (IOException e) {
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                skt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class AddMes implements Runnable{

        String s;

        AddMes(String s){
            this.s = s;
        }

        @Override
        public void run() {
            messageModel.addElement(s);
        }
    }

}
