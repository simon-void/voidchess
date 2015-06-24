/*
 * Created on 01.11.2006
 */

package organisation;

import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import board.ChessGameInterface;
import player.ki.*;

public class DifficultyPanel
extends JPanel
implements ActionListener
{
	final private static String LEVEL1_TEXT = "Stufe 1";
	final private static String LEVEL2_TEXT = "Stufe 2";
	final private static String LEVEL3_TEXT = "Stufe 3";
	
	final private ComputerPlayer player;
	final private SimplePruner level_1_pruner,level_2_pruner,level_3_pruner;
	
	final private JComboBox comboBox;
	
	public DifficultyPanel( ComputerPlayer player,ChessGameInterface game )
	{
		level_1_pruner = new SimplePruner( 1,4,2 );
		level_2_pruner = new SimplePruner( 2,3,2 );
		level_3_pruner = new SimplePruner( 2,4,3 );
		this.player = player;
		player.setSearchTreePruner( level_1_pruner );
		
		comboBox = new JComboBox();
		designLayout();
	}
	
	private void designLayout()
	{
		setBackground( Color.WHITE );
		setBorder( new LineBorder( Color.LIGHT_GRAY ) );
		
		comboBox.addItem( LEVEL1_TEXT );
		comboBox.addItem( LEVEL2_TEXT );
		comboBox.addItem( LEVEL3_TEXT );
		comboBox.setEditable( false );
		comboBox.addActionListener( this );
		
		add( new JLabel( "Schwirigkeitsgrad:" ) );
		add( comboBox );
	}
	
	public void setEnabled( boolean enable )
	{
		comboBox.setEnabled( enable );
	}
	
	public void actionPerformed( ActionEvent event )
	{
		if( comboBox.getSelectedItem().equals( LEVEL1_TEXT ) ) {
			player.setSearchTreePruner( level_1_pruner );
		}else if( comboBox.getSelectedItem().equals( LEVEL2_TEXT ) ) {
			player.setSearchTreePruner( level_2_pruner );
		}else if( comboBox.getSelectedItem().equals( LEVEL3_TEXT ) ) {
			player.setSearchTreePruner( level_3_pruner );
		}
	}

}
