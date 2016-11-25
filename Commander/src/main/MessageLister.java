package main;

import Client.commands.AddrStruct;
import Client.messages.Address;
import Client.messages.PeerAddress;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by machiara on 22/11/16.
 */
public class MessageLister extends JPanel implements ListCellRenderer<AddrStruct> {


    public static DefaultListModel<String> messageModel;

    public static DefaultListModel<AddrStruct> addressModel;

    MessageLister(Socket s,Socket s1){
        messageModel = new DefaultListModel<>();
        addressModel = new DefaultListModel<>();

        JList<String> list = new JList<>(messageModel);
        JList<AddrStruct> list1 = new JList<>(addressModel);

        list1.setCellRenderer(this);

        JScrollPane panel = new JScrollPane(list);
        JScrollPane panel1 = new JScrollPane(list1);


        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,450));
        panel1.setMaximumSize(new Dimension(Integer.MAX_VALUE,450));
        BoxLayout layout = new BoxLayout(this,BoxLayout.Y_AXIS);
        setLayout(layout);
        add(panel);
        add(panel1);
        new Thread(new MessageHandler(s)).start();
        new Thread(new AddressHandler(s1)).start();
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends AddrStruct> list, AddrStruct value, int index, boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel();
        JLabel addr = new JLabel();
        JLabel sent = new JLabel();
        InetAddress a = Main.addressSent.get(value.addr);
        if(a != null)
        {
            addr.setForeground(Color.green);
            sent.setForeground(Color.green);
        }
        sent.setText("Ricevuto da: "+value.peer);
        addr.setText(String.valueOf(value.addr));

        panel.setOpaque(true);
        panel.add(sent);
        panel.add(addr);
        if(a != null)
        {
            JLabel l = new JLabel("Inviato a: "+a);
            panel.add(l);
        }

        return panel;

    }

    private class MessageHandler implements Runnable{

        Socket skt;

        MessageHandler(Socket s){
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

    private class AddressHandler implements Runnable {

        Socket skt;

        public AddressHandler(Socket s1) {
            skt = s1;
        }


        @Override
        public void run() {
            ObjectInputStream in = null;
            try{
                in = new ObjectInputStream(skt.getInputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            while(true)
            {
                try
                {
                    Address addr = (Address) in.readUnshared();
                    String peer = (String) in.readUnshared();
                    SwingUtilities.invokeLater(new AddrAdder(addr,peer));
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }

        private class AddrAdder implements Runnable {

            Address addr;
            String peer;

            public AddrAdder(Address addr, String peer) {
                this.addr = addr;
                this.peer = peer;
            }

            @Override
            public void run() {
                for(PeerAddress pa : addr.getAddresses())
                    if(Main.addressSent.containsKey(pa))
                        addressModel.addElement(new AddrStruct(pa,peer));
            }
        }
    }


}
