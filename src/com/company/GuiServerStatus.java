package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by vlad on 31.03.2017.
 */
public class GuiServerStatus extends JFrame {
    private static GuiServerStatus instance = new GuiServerStatus();
    private HashMap<Integer, Socket> hm;
    private JLabel lTimeOfWorking;
    private long time = 0;
    private JTextArea   jtaLog;


    private class ActionClose extends WindowAdapter {
        public void windowClosing(WindowEvent e){
            for (Socket s: hm.values()) {
                if (s.isConnected()) try {
                    s.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            System.exit(0);
        }
    }

    private GuiServerStatus() {
        super("Server setting");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 800, 500);
        setMinimumSize(new Dimension(400, 500));
        addWindowListener(new ActionClose());
        setLAF();
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        lTimeOfWorking = new JLabel("Server operating time:  0:0:0");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    time++;
                    lTimeOfWorking.setText("Server operating time: " + time / 3600 + ":" + (time % 3600) / 60 + ":" + (time % 3600) % 60);
                    //lTimeOfWorking.revalidate();
                    //lTimeOfWorking.repaint();
                }
            }
        }).start();




        JTabbedPane jtb = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);

        jtaLog = new JTextArea();
        jtaLog.setEditable(false);
        JScrollPane jspForLog = new JScrollPane(jtaLog,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        jtb.addTab("Log", jspForLog);




        contentPanel.add(lTimeOfWorking);
        contentPanel.add(jtb);
        setContentPane(contentPanel);
    }
    private void setLAF() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToLog(String str) {
        jtaLog.append(str);
        jtaLog.append("\n\n");

    }
    public static GuiServerStatus getInstance() {
        return instance;
    }
    public void setHm(HashMap<Integer, Socket> hm) {
        this.hm = hm;
    }





}
