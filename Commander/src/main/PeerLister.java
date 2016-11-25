package main;

import Client.commands.Send;
import Client.commands.Show;
import Client.commands.Update;
import Client.messages.Address;
import Client.messages.PeerAddress;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by machiara on 22/11/16.
 */
public class PeerLister extends JPanel implements ListCellRenderer<String>, ActionListener {


    public static DefaultListModel<String> peersModel;

    public static JList<String> peers;

    public static int sum = 0;

    public PeerLister(){
        peersModel = new DefaultListModel<>();
        peers = new JList<>(peersModel);
        peers.setCellRenderer(this);
        JScrollPane peerList = new JScrollPane(peers);
        peerList.setMaximumSize(new Dimension(320,Integer.MAX_VALUE));

        JButton refresh = new JButton("Refresh");
        JButton see = new JButton("Show");
        JButton send = new JButton("Send Address");

        refresh.setActionCommand("refresh");
        see.setActionCommand("show");
        send.setActionCommand("send");

        refresh.addActionListener(this);
        see.addActionListener(this);
        send.addActionListener(this);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        buttons.add(refresh);
        buttons.add(see);
        buttons.add(send);
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        add(peerList);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(buttons);

    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel name = new JLabel(value);
        if (isSelected)
        {
            name.setBackground(list.getSelectionBackground());
            name.setForeground(list.getSelectionForeground());
        } else
        {
            name.setBackground(list.getBackground());
            name.setForeground(list.getForeground());
        }
        name.setEnabled(list.isEnabled());
        name.setOpaque(true);
        return name;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand())
        {
            case "refresh" :
                CommandTask<Update,RefreshTask> ct = new CommandTask<>();
                ct.command = new Update();
                ct.task = new RefreshTask();
                Main.commands.add(ct);
                break;
            case "show" :
                CommandTask<Show, NullTask> c = new CommandTask<>();
                try {
                    c.command = new Show(InetAddress.getByName(peers.getSelectedValue().split("/")[0]));
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
                c.task = new NullTask();
                Main.commands.add(c);
                break;
            case "send" :
                if(peers.getSelectedValue() != null)
                {
                    InetAddress addr = null;
                    try
                    {
                        addr = InetAddress.getByName(peers.getSelectedValue().split("/")[0]);
                    } catch (UnknownHostException e1)
                    {
                        e1.printStackTrace();
                    }
                    CommandTask<Send, NullTask> command = new CommandTask<>();
                    Address m = createAddress(addr);
                    List<InetAddress> list = new ArrayList<>();
                    list.add(addr);
                    Send msg= new Send(m,list);
                    command.command = msg;
                    command.task = new NullTask();
                    Main.commands.add(command);
                    break;
                }
        }
    }

    private Address createAddress(InetAddress a) {
        Address m = new Address();
        List<PeerAddress> addresses = m.getAddresses();
        for(int i = 0; i < 1000; i++)
        {
            PeerAddress addr = new PeerAddress();
            try
            {
                addr.setAddress(InetAddress.getByName("100.100.100.100"));
            } catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            addr.setPort(i+3000+sum);
            addr.setService(2);
            addr.setTime((int) (System.currentTimeMillis() / 1000) - 1000*60*10);
            addresses.add(addr);
            Main.addressSent.put(addr,a);
        }
        sum+=1000;
        return m;
    }
}
