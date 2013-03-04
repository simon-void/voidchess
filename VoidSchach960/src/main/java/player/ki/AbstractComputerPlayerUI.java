package player.ki;

import helper.Move;

import javax.swing.JComponent;

/**
 * @author stephan
 */
public abstract class AbstractComputerPlayerUI extends JComponent
{
	abstract public void setProgress( int part,int whole );
	abstract public void setValue( float value,Move move );
}
