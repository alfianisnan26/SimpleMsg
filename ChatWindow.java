import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ChatWindow implements ActionListener {
    private volatile boolean exit = false;
    private JFrame frame;
    private JButton sendButton;
    private JButton clearButton;
    private JMenuBar menuBar;
    private JTextField textField;
    private JPanel mainPanel;
    private int chatCount = 0;
    private JScrollPane scrollPanel;
    private ChatListener chatListener;
    private Thread threadListener;
    public  DataAccount data;

    public ChatWindow(String sender, String receiver) {
        loadWindow(receiver);
        loadUI();
        this.data = new DataAccount(sender, receiver);
        Database.initChat(data);
        System.out.println("New Chat Window: " + sender + "\nSharing Hash key: " + data.IDHash);
    }

    public void show() {
        frame.setVisible(true);
        chatListener = new ChatListener();
    }

    private void loadWindow(String title) {
        frame = new JFrame("Chat Room: " + title);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                threadListener = new Thread(chatListener, data.receiver);
                threadListener.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                exit = true;
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
                if (exit) {
                    threadListener = new Thread(chatListener, data.receiver);
                    threadListener.start();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem menuExit = new JMenuItem("Exit");
        JMenuItem menuClear = new JMenuItem("Clear All");
        JMenuItem menuAbout = new JMenuItem("About");
        menuExit.addActionListener(this);
        menuClear.addActionListener(this);
        menuAbout.addActionListener(this);
        menu.add(menuClear);
        menu.addSeparator();
        menu.add(menuAbout);
        menu.add(menuExit);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        frame.setSize(350, 600);
        frame.setLocationRelativeTo(null);
    }

    private void loadUI() {
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        clearButton = new JButton("Clear");
        textField = new JTextField(256);
        textField.addActionListener(this);
        clearButton.addActionListener(this);
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        panel2.add(sendButton);
        panel2.add(clearButton);
        panel1.add(textField);
        panel1.add(panel2);
        mainPanel = new JPanel();
        scrollPanel = new JScrollPane(mainPanel);
        scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPanel.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            private int isChange;

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (isChange != e.getAdjustable().getMaximum()) {
                    isChange = e.getAdjustable().getMaximum();
                    e.getAdjustable().setValue(isChange);
                }
            }
        });
        frame.getContentPane().add(BorderLayout.CENTER, scrollPanel);
        frame.getContentPane().add(BorderLayout.SOUTH, panel1);
    }

    public void dispose() {
        frame.dispose();
        exit = true;
    }

    private void addChat(JLabel myText) {
        chatCount++;
        mainPanel.setLayout(new GridLayout((chatCount < 12) ? 12 : chatCount, 2));
        myText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(myText);
        mainPanel.revalidate();
        mainPanel.repaint();
        textField.setText("");
        textField.requestFocus();
    }

    public void actionPerformed(ActionEvent e) {
        String str = e.getActionCommand();
        System.out.println(str);
        if (str.equals("Exit")) {
            System.exit(0);
        } else if (str.equals("Clear All")) {
            chatCount = 0;
            mainPanel.removeAll();
            mainPanel.setLayout(new GridLayout(12, 1));
            scrollPanel.revalidate();
            scrollPanel.repaint();
        } else if (str.equals("Send") || e.getModifiers() == 0) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            JLabel myText = new JLabel(dtf.format(now) + data.sender + " : " + textField.getText(), JLabel.LEFT);
            myText.setForeground(Color.red);
            if (Database.sendChat(data, myText.getText())) {
                addChat(myText);
            } else {
                Dialogs d = new Dialogs("Message could not be delivered");
                d.show();
            }
        } else if (str.equals("Clear")) {
            textField.setText("");
            textField.requestFocus();
        }else if (str.equals("About")){
            Dialogs d = new Dialogs("SimpleMsg by Kelompok 4-OOP");
            d.show();
        }
    }

    private class ChatListener implements Runnable {
        @Override
        public void run() {
            System.out.println("Listening to user: " + data.receiver + "...");
            exit = false;
            while (!exit) {
                String in = Database.getChat(data);
                if (!in.equals("null")) {
                    System.out.println(in);
                    in = in.substring(1, in.length() - 1);
                    JLabel myText = new JLabel(in, JLabel.LEFT);
                    myText.setForeground(Color.blue);
                    addChat(myText);
                }else{
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Chat Listener is stopped....");
            return;
        }
    }
}