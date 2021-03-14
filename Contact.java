import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.*;

public class Contact{
    public JScrollPane scrollPanel;
    public JPanel mainPanel = new JPanel();
    private ChatWindow[] window = new ChatWindow[128];
    private int count = 0;
    private String sender;
    public Contact(String sender){
        this.sender = sender;
    }
    public JScrollPane panelInit(){
        scrollPanel = new JScrollPane(mainPanel);
        scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPanel.getVerticalScrollBar().addAdjustmentListener((AdjustmentListener) new AdjustmentListener() {  
            private int isChange;
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if(isChange != e.getAdjustable().getMaximum()){
                    isChange = e.getAdjustable().getMaximum();
                    e.getAdjustable().setValue(isChange);  
                }
            }
        });
        return scrollPanel;
    }
    private void mainUpdate(){
        mainPanel.setLayout((LayoutManager) new GridLayout((count<7)?7:count, 1));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void addPanel(ChatWindow chat){
        JButton btn1 = new JButton(chat.data.receiver);
        JButton btn2 = new JButton("X");
        JPanel panel = new JPanel();
        btn1.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                chat.show();
            }
            
        });
        btn2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int a=0;a<count;a++){
                    if(window[a].equals(chat)){
                        System.out.println("Found At " + a);
                        window[a].dispose();
                        mainPanel.remove(a);
                        for(int b=a;b<count-1;b++){
                            window[b] = window[b+1];
                        }
                        count--;
                        mainUpdate();
                        return;
                    }
                }
            }
        });
        btn1.setPreferredSize(new Dimension(200,25));
        btn2.setBackground(Color.red);
        btn2.setForeground(Color.white);
        panel.add(btn1);
        panel.add(btn2);
        mainPanel.add(panel);
    }

    public boolean uploadContact(){
        String out = new String();
        for(int a=0;a<count;a++){
            out = out.concat(window[a].data.receiver + " ");
        }
        System.out.println("Saved Contact : " + count + "\n" + out);
        return Database.writeContact(sender, out);
    }

    public boolean downloadContact(){
        String in = Database.getContact(sender);
        String[] arrContact = in.split(" ");
        for(int a=0;a<arrContact.length;a++){
            this.add(arrContact[a]);
        }
        System.out.println("Loaded Contact : " + count + "\n" + in);
        return true;
    }

    public String add(String receiver){
        if(count > 127) return "Maximum contact exceeded";
        for(int a=0;a<count;a++){
            if(window[a].data.receiver.equals(receiver)){
                return "User Existed";
            }
        }
        if(receiver.contains(" ") || receiver.equals(sender)){
            return "Please input correct username";
        }
        if(Database.checkUser(receiver)){
            return "User Not Found";
        }
        window[count] = new ChatWindow(sender, receiver);
        addPanel(window[count]);
        count++;
        mainUpdate();
        return "Add new username here";
    }

}
