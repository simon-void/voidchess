package voidchess.player.ki;

import voidchess.helper.Move;
import voidchess.player.ki.evaluation.Evaluated;

import javax.swing.*;

/**
 * @author stephan
 */
public abstract class AbstractComputerPlayerUI extends JComponent {
    abstract public void setProgress(int part, int whole);

    abstract public void setValue(Evaluated value, Move move);
}
