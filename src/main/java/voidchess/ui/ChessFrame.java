package voidchess.ui;

import voidchess.image.ImageLoader;

import javax.swing.*;
import java.awt.*;


public class ChessFrame extends JFrame {
    public static void main(String[] args) {
        try {
            //Swing UI updates have to come from the SwingHandler or something
            SwingUtilities.invokeLater(
                    ChessFrame::new
            );
        } catch (Exception e) {
            String sb = "The game got cancled because of an error.\n" +
                        "The error message is:\n" + e.toString();
            JOptionPane.showMessageDialog(null, sb);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private ChessFrame() {
        super("  VoidChess960  ");
        setIconImage(ImageLoader.INSTANCE.getIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new ChessPanel());
        pack();
        setResizable(false);
        center();
        setVisible(true);
    }

    private void center() {
        try{
            Dimension frameSize = getSize();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle windowSize = ge.getMaximumWindowBounds();
            setLocation(Math.max(windowSize.width/2-frameSize.width/2, 0), Math.max(windowSize.height/2-frameSize.height/2, 0));
        }catch (RuntimeException e) {
            // best effort
        }
    }
}
