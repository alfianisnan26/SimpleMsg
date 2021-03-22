import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.*;

public class ChatWindow implements ActionListener {
    private volatile boolean exit = false;
    private JFrame frame;
    private JButton sendButton;
    private JButton attachButton;
    private JButton clearButton;
    private JMenuBar menuBar;
    private JTextField textField;
    private JPanel mainPanel;
    private int chatCount = 0;
    private JScrollPane scrollPanel;
    private ChatListener chatListener;
    private Thread threadListener;
    private File file;
    public DataAccount data;

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
        attachButton = new JButton("Attach");
        textField.addActionListener(this);
        clearButton.addActionListener(this);
        attachButton.addActionListener(this);
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel2.setLayout(new GridLayout(3, 1));
        panel2.add(sendButton);
        panel2.add(attachButton);
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

    private void mainFrameRevalidate() {
        mainPanel.setLayout(new GridLayout((chatCount < 11) ? 11 : chatCount, 1));
        mainPanel.revalidate();
        mainPanel.repaint();
        textField.setText("");
        textField.requestFocus();
    }

    private void addChat(JLabel myText) {
        chatCount++;
        myText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(myText);

    }

    private void addFile(File file, String url, String name) {
        chatCount++;
        JPanel buttonPanel = new JPanel();
        JLabel label = new JLabel(name);
        JButton openFile = new JButton("Open");
        if(file == null){
            openFile.setText("Download");
        }
        openFile.addActionListener(new ActionListener() {
            File myFile = file;
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(openFile)) {
                    if(myFile==null){
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fileChooser.setSelectedFile(new File("./"+name));
                        int val = fileChooser.showSaveDialog(frame);
                        if (val == JFileChooser.APPROVE_OPTION) {
                            myFile = fileChooser.getSelectedFile();
                            Dialogs d = new Dialogs("Downloading...");
                            d.start();
                            boolean a = Database.downloadFile(url, myFile);
                            d.dispose();
                            if(!a){
                                myFile = null;
                                new Dialogs("Download Error").start();
                                return;
                            }else{
                                openFile.setText("Open");
                                openFile.revalidate();
                                openFile.repaint();
                            }
                        }
                        else{
                            myFile = null;
                            return;
                        }
                    }
                    if(myFile.exists() && myFile != null){
                         Database.open(myFile);
                    }else{
                        new Dialogs("File not found...").start();
                        return;
                    }
                }
            }
        });
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(label);
        buttonPanel.add(openFile);
        buttonPanel.setPreferredSize(new Dimension(10, 20));
        mainPanel.add(buttonPanel);
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
            if (textField.getText().length() == 0) {
                return;
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String text = textField.getText();
            String url = "";
            Dialogs k = new Dialogs("Wait for uploading...");
            if (text.charAt(0) == '~' && file != null) {
                k.start();
                System.out.println("Attachment Detect");
                url = "@" + file.getName() + "~" + Database.uploadFile(this.file) + "~";
                text = text.substring(1);
                k.dispose();
            }
            JLabel myText = new JLabel(dtf.format(now) + data.sender + " : " + text, JLabel.LEFT);
            myText.setForeground(Color.red);
            if (Database.sendChat(data, url + myText.getText())) {
                addChat(myText);
                if (!url.equals(""))
                    addFile(this.file, null, this.file.getName());
                mainFrameRevalidate();
            } else {
                new Dialogs("Message could not be delivered").start();
            }
        } else if (str.equals("Clear")) {
            textField.setText("");
            textField.requestFocus();
        } else if (str.equals("About")) {
            new Dialogs("SimpleMsg by Kelompok 4-OOP").start();;
        } else if (e.getSource() == attachButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int val = fileChooser.showOpenDialog(frame);
            if (val == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                long a = file.length();
                System.out.println("Size:" + a + " | " + a / 1000000.0 + "MB");
                if (file.length() > 1024 * 5000000) {
                    new Dialogs("Cannot send files larger than 5MB").start();
                } else {
                    textField.setText("~" + textField.getText());
                    textField.setCaretPosition(textField.getText().length());
                    textField.requestFocus();
                }
            }
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
                    boolean isFile = false;
                    System.out.println(in);
                    in = in.substring(1, in.length() - 1);
                    String url = null;
                    String name = null;
                    if(in.charAt(0) == '@'){
                        isFile = true;
                        String[] splitter = in.split("~");
                        System.out.println("Attachment Detect Split: " + splitter.length);
                        url = splitter[1];
                        name = splitter[0].substring(1);
                        String out = "";
                        for(int a=2;a<splitter.length;a++){
                            out = out.concat(splitter[a]);
                        }
                        System.out.println("Chat : " + out);
                        in = out;
                    }
                    JLabel myText = new JLabel(in, JLabel.LEFT);
                    myText.setForeground(Color.blue);
                    addChat(myText);
                    if(isFile){
                        addFile(null, url, name);
                    }
                    mainFrameRevalidate();
                } else {
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