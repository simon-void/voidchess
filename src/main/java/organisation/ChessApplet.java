/*
 * Created on 11.09.2006
 */

package organisation;

import image.Images;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ChessApplet
        extends JApplet {
    final public static int APPLET_STOPPED = -100;
    private ChessPanel chessPanel;

    public void init() {
        try {
            Images.loadImageResources();
            chessPanel = new ChessPanel();
            chessPanel.setBackground(Color.white);
            chessPanel.setBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(
                                    Color.black
                            ),
                            "VoidSchach960 Applet"
                    )
            );
            setContentPane(chessPanel);
            setSize(getPreferredSize());
        } catch (IOException e) {
            displayException(e);
        }
    }

    public void start() {
        try {
            helper.RuntimeFacade.assertJavaVersion();
            setVisible(true);
        } catch (RuntimeException e) {
            displayException(e);
        }
    }

    public void stop() {
        chessPanel.stop(APPLET_STOPPED);
        setVisible(false);
    }

    private void displayException(Exception e) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Das Spiel wurde aufgrund eines Fehlers abgebrochen.");
        sb.append("\n");
        sb.append("Die Fehlermeldung lautet:");
        sb.append("\n");
        sb.append(e.toString());
        JOptionPane.showMessageDialog(null, sb.toString());
    }
}
