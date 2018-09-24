package voidchess.ui;

import voidchess.image.ImageLoader;

import javax.swing.*;


public class ChessFrame extends JFrame {
    public static void main(String[] args) {
        try {
            voidchess.helper.RuntimeFacade.assertJavaVersion();
            ImageLoader.loadImageResources();
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
        setIconImage(ImageLoader.getIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new ChessPanel());
        pack();
        setResizable(false);
        setVisible(true);
    }
}
