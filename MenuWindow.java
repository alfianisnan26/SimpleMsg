import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuWindow implements ActionListener{
    private JTextField unameTextField;
    private JPasswordField passTextField;
    private JFrame loginFrame;
    private JLabel alertLabel;
    public void Login(){
        loginFrame = new JFrame("Login");
        JLabel appLabel = new JLabel("Simple Instant Messaging App", JLabel.CENTER);
        loginFrame.setResizable(false);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400,200);
        loginFrame.setLocationRelativeTo(null);
        JPanel uname = new JPanel();
        JPanel pass = new JPanel();
        JPanel button = new JPanel();
        JPanel mainPanel = new JPanel();
        alertLabel = new JLabel("Input your credential", JLabel.CENTER);
        alertLabel.setForeground(Color.RED);
        alertLabel.setFont(new Font("Sans Serif", Font.PLAIN, 10));
        uname.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        pass.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        unameTextField = new JTextField(36);
        unameTextField.setText("@");
        unameTextField.setCaretPosition(1);
        unameTextField.addActionListener(this);
        passTextField = new JPasswordField(36);
        passTextField.addActionListener(this);
        JLabel unameL = new JLabel("Username:   ");
        JLabel passL = new JLabel( "Password:   ");
        JButton signUp = new JButton("Sign Up");
        JButton login = new JButton("Login");
        signUp.addActionListener(this);
        login.addActionListener(this);
        uname.setLayout( new BoxLayout( uname, BoxLayout.X_AXIS) );  
        pass.setLayout( new BoxLayout( pass, BoxLayout.X_AXIS) );  
        button.setLayout(new GridLayout(1,2));  
        mainPanel.setLayout(new GridLayout(5,1));
        uname.add(unameL);
        uname.add(unameTextField);
        pass.add(passL);
        pass.add(passTextField);
        button.add(signUp);
        button.add(login);
        mainPanel.add(appLabel);
        mainPanel.add(alertLabel);
        mainPanel.add(uname);
        mainPanel.add(pass);
        mainPanel.add(button);
        loginFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        loginFrame.setVisible(true);
    }

    private void alertUpdate(String msg){
        passTextField.setText("");
        unameTextField.requestFocus();
        alertLabel.setText(msg);
        loginFrame.revalidate();
        loginFrame.repaint();
    }
    
    

    @Override
    public void actionPerformed(ActionEvent e) {
        String str = e.getActionCommand();
        int val = e.getModifiers();
        System.out.println(val);
        if(val==0 || str.equals("Login") || str.equals("Sign Up")){
            if(unameTextField.getText().length()<4){
                alertUpdate("Please input corrent Username");
                
                return;
            }
            if(unameTextField.getText().contains(" ")){
                alertUpdate("Please input corrent Username");
                
                return;
            }
            else if(passTextField.getPassword().length<4){
                alertUpdate("Please input corrent Password");
                
                return;
            }
            else if(str.equals("Sign Up")){
                if(Database.checkUser(unameTextField.getText())){
                    if(!Database.createUser(unameTextField.getText(), passTextField.getPassword())){
                        
                        return;
                    }
                }else{
                    alertUpdate("Username is not available");
                    return;
                }
            }
            if(Database.loginUser(unameTextField.getText(), passTextField.getPassword())){
                new ContactWindow(unameTextField.getText());
                loginFrame.setVisible(false);
                loginFrame.dispose();
                return;
            }else{
                alertUpdate("Error Cannot Login, Please Retry");
                
                return;
            }
        }
    }
}
