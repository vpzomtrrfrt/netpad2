package vpzomtrrfrt.netpad2.exampleclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        try {
            String[] opts = {"Load", "New", "Cancel"};
            Integer[] btns;
            Pair<Integer>[] axis;
            while(true) {
                int n = JOptionPane.showOptionDialog(null, "Create or load a configuration", "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
                if (n == 0) {
                    JFileChooser fc = new JFileChooser();
                    int r = fc.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        File f = fc.getSelectedFile();
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                        String[] btnStrings = br.readLine().split(",");
                        btns = new Integer[btnStrings.length];
                        for(int i = 0; i < btns.length; i++) {
                            btns[i] = Integer.parseInt(btnStrings[i]);
                        }
                        List<Pair<Integer>> l = new ArrayList<Pair<Integer>>();
                        String line = br.readLine();
                        while(line != null) {
                            String[] spl = line.split(",");
                            Pair<Integer> ta = new Pair<Integer>(Integer.parseInt(spl[0]), Integer.parseInt(spl[1]));
                            l.add(ta);
                            line = br.readLine();
                        }
                        axis = l.toArray(new Pair[0]);
                        break;
                    }
                }
                else if(n == 1) {
                    btns = new Integer[0];
                    axis = new Pair[0];
                    break;
                }
                else {
                    System.exit(0);
                }
            }
            final JFrame cfgFrame = new JFrame();
            final ConfigPanel cfgPanel = new ConfigPanel(btns, axis);
            cfgFrame.add(cfgPanel);
            JPanel cfgBtns = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton okBtn = new JButton("OK");
            okBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cfgFrame.setVisible(false);
                    String addr = JOptionPane.showInputDialog("Enter server address:");
                    OutputStream os;
                    while(true) {
                        try {
                            Socket sock = new Socket(addr, 4242);
                            os = sock.getOutputStream();
                            break;
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            JOptionPane.showMessageDialog(null, e1.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    theThing(os, cfgPanel.getBtns(), cfgPanel.getAxis());
                }
            });
            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    int r = fc.showSaveDialog(cfgFrame);
                    if(r == JFileChooser.APPROVE_OPTION) {
                        File f = fc.getSelectedFile();
                        Integer[] btns = cfgPanel.getBtns();
                        Pair<Integer>[] axis = cfgPanel.getAxis();
                        try {
                            FileOutputStream fos = new FileOutputStream(f);
                            OutputStreamWriter sw = new OutputStreamWriter(fos);
                            boolean first = true;
                            for(Integer btn : btns) {
                                if(!first) {
                                    sw.write(',');
                                }
                                else {
                                    first = false;
                                }
                                sw.write(btn.toString());
                            }
                            sw.write('\n');
                            for(Pair<Integer> a : axis) {
                                sw.write(a.getFirst()+","+a.getLast()+"\n");
                            }
                            sw.close();
                        } catch(Exception e2) {
                            e2.printStackTrace();
                            JOptionPane.showMessageDialog(null, e2.getMessage(), "Save Failure", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            cfgBtns.add(saveBtn);
            cfgBtns.add(okBtn);
            cfgFrame.add(cfgBtns, BorderLayout.SOUTH);

            cfgFrame.setVisible(true);
            cfgFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void theThing(OutputStream os, final Integer[] btns, final Pair<Integer>[] axis) {
        final OutputStreamWriter bw = new OutputStreamWriter(os);
        try {
            bw.write(axis.length + "," + btns.length + "\n");
            bw.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame();
        frame.addKeyListener(new KeyListener() {
            private int[] av = new int[axis.length];

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode(), 1);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKey(e.getKeyCode(), 0);
            }

            private void handleKey(int key, int val) {
                try {
                    for(int i = 0; i < btns.length; i++) {
                        if(key == btns[i]) {
                            bw.write("B"+i+":"+val+"\n");
                        }
                    }
                    for(int i = 0; i < axis.length; i++) {
                        int a = 0;
                        if(key == axis[i].getFirst()) {
                            a--;
                        }
                        else if(key == axis[i].getLast()) {
                            a++;
                        }
                        if(a != 0) {
                            if(val==0) {
                                av[i] -= a;
                            }
                            else {
                                av[i] += a;
                            }
                            bw.write("A"+i+":"+av[i]+"\n");
                        }
                    }
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
        frame.setSize(200, 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}