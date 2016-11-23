package main;

import Client.commands.Show;
import Client.commands.Update;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by machiara on 22/11/16.
 */
public class PeerLister extends JPanel implements ListCellRenderer<String>, ActionListener {


    public static DefaultListModel<String> peersModel;

    public static JList<String> peers;

    public PeerLister(){
        peersModel = new DefaultListModel<>();
        peers = new JList<>(peersModel);
        peers.setCellRenderer(this);
        JScrollPane peerList = new JScrollPane(peers);
        peerList.setMaximumSize(new Dimension(320,Integer.MAX_VALUE));

        JButton refresh = new JButton("Refresh");
        JButton see = new JButton("Show");

        refresh.setActionCommand("refresh");
        see.setActionCommand("show");

        refresh.addActionListener(this);
        see.addActionListener(this);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        buttons.add(refresh);
        buttons.add(see);
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        add(peerList);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(buttons);

    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        JPanel p = new JPanel();
        JLabel name = new JLabel(value);
        if (isSelected)
        {
            p.setBackground(list.getSelectionBackground());
            p.setForeground(list.getSelectionForeground());
        } else
        {
            p.setBackground(list.getBackground());
            p.setForeground(list.getForeground());
        }
        name.alig
        p.setEnabled(list.isEnabled());
        p.setOpaque(true);
        p.add(name);
        return p;
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
                    c.command = new Show(InetAddress.getByName(peers.getSelectedValue().split("/")[0]),MessageLister.skt.getInetAddress());
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
                c.task = new NullTask();
                Main.commands.add(c);
        }
    }
}
