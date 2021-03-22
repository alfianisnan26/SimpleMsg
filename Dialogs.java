import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Dialogs {
    
    String str;
    public JDialog error;
    public Dialogs(String str) {
        this.str = str;
        error = new JDialog();
        error.add(new JLabel(str, JLabel.CENTER));
        error.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        error.setLocationRelativeTo(null);
        error.setSize(250, 75);
        error.revalidate();
    }

    public void start(){
        error.setVisible(true);
    }

    public void dispose(){
        error.dispose();
    }
}
