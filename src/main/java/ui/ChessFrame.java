package ui;

import image.ImageType;
import image.Images;

import javax.swing.*;

/**
 * @author stephan
 */
public class ChessFrame extends JFrame {
    public static void main(String[] args) {
        try {
            helper.RuntimeFacade.assertJavaVersion();
            Images.loadImageResources();
            //Swing UI updates have to come from the SwingHandler or something
            SwingUtilities.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            new ChessFrame();
                        }
                    }
            );
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(64);
            sb.append("Das Spiel wurde aufgrund eines Fehlers abgebrochen.");
            sb.append("\n");
            sb.append("Die Fehlermeldung lautet:");
            sb.append("\n");
            sb.append(e.toString());
            JOptionPane.showMessageDialog(null, sb.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public ChessFrame() {
        super("  VoidSchach960  ");
        setIconImage(Images.get(ImageType.ICON));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new ChessPanel());
        pack();
        setResizable(false);
        setVisible(true);
    }


}
