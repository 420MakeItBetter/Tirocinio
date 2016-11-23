package main;

import Client.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by machiara on 22/11/16.
 */
public class ClientGui extends JFrame implements ActionListener {

    JTextField text;

    public ClientGui(){
        setSize(1000,1000);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setTitle("Commander");
        setVisible(true);
        JButton but = new JButton();
        but.setText("Connetti");
        but.addActionListener(this);
        text = new JTextField();
        text.setColumns(50);
        this.add(text);
        this.add(but);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            Socket s = new Socket();
        try {
            Main.skt.connect(new InetSocketAddress(InetAddress.getByName(text.getText()),4201));
            s.connect(new InetSocketAddress(InetAddress.getByName(text.getText()),5000));
            Main.in = new ObjectInputStream(Main.skt.getInputStream());
            Main.out = new ObjectOutputStream(Main.skt.getOutputStream());
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(this,"Errore di connessione","Error",JOptionPane.ERROR_MESSAGE);
            try {
                Main.skt.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            try {
                s.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            Main.skt = new Socket();
            return;
        }
        SwingUtilities.invokeLater(new MainGui(s));
    }
}
