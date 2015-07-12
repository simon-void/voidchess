package player.ki;

import helper.Move;

import javax.swing.*;

/**
 * @author stephan
 */
public abstract class AbstractComputerPlayerUI extends JComponent {
    abstract public void setProgress(int part, int whole);

    abstract public void setValue(Evaluaded value, Move move);
}
