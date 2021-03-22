import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ContactWindow implements ActionListener {
    private JFrame frame;
    private JButton addButton;
    private JLabel alertLabel;
    private JMenuBar menuBar;
    private JTextField textField;
    private String username;
    private Contact contact;
    public ContactWindow(String username){
        this.username = username;
        contact = new Contact(this.username);
        contact.downloadContact();
        loadWindow(username + " Contact");
        loadUI();
        frame.addWindowListener(new WindowListener(){

            @Override
            public void windowOpened(WindowEvent e) {
                
            }

            @Override
            public void windowClosing(WindowEvent e) {
                contact.uploadContact();
                
            }

            @Override
            public void windowClosed(WindowEvent e) {
                
            }

            @Override
            public void windowIconified(WindowEvent e) {
                
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                
            }

            @Override
            public void windowActivated(WindowEvent e) {
             
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                
            }
            
        });
        frame.setVisible(true);
    }

    private void loadWindow(String title){
        frame = new JFrame(title);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.addActionListener(this);
        menu.add(menuExit);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        frame.setSize(300,400);
    }

    private void loadUI(){
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JLabel unameL = new JLabel("Username:");
        addButton = new JButton("Add");
        addButton.addActionListener(this);
        alertLabel = new JLabel("Add new username here");
        alertLabel.setForeground(Color.RED);
        alertLabel.setFont(new Font("Sans Serif", Font.PLAIN, 10));
        textField = new JTextField(256);
        textField.addActionListener(this);
        textField.setText("@");
        textField.setCaretPosition(1);
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        panel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));  
        panel2.setLayout(new BoxLayout(panel2,BoxLayout.X_AXIS));  
        panel2.add(unameL);
        panel2.add(textField);
        panel2.add(addButton);
        panel1.add(alertLabel);
        panel1.add(panel2);
        frame.getContentPane().add(BorderLayout.CENTER, contact.panelInit());
        frame.getContentPane().add(BorderLayout.SOUTH, panel1);
    }

    public void actionPerformed(ActionEvent e){
        String str = e.getActionCommand();
        System.out.println(str + " | " +  Integer.toString(e.getModifiers()));
        if(str.equals("Exit")){
            System.exit(0);
        }else if(str.equals("Add") || e.getModifiers() == 0){
            alertLabel.setText(contact.add(textField.getText()));
            textField.setText("@");
            textField.setCaretPosition(1);
        }
    }
}