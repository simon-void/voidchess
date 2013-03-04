package organisation;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import image.*;
/**
 * @author stephan
 */
public class ChessFrame extends JFrame
{
	public static void main( String[] args )
	{
		try {
			helper.RuntimeFacade.assertJavaVersion();
			Images.init(new JPanel());
			new ChessFrame();
		}catch( Exception e ) {
			StringBuilder sb = new StringBuilder( 64 );
			sb.append( "Das Spiel wurde aufgrund eines Fehlers abgebrochen." );
			sb.append( "\n" );
			sb.append( "Die Fehlermeldung lautet:" );
			sb.append( "\n" );
			sb.append( e.toString() );
			JOptionPane.showMessageDialog( null,sb.toString() );
			e.printStackTrace();
			System.exit( 1 );
		}
	}
	
	public ChessFrame()
	{
		super( "  VoidSchach960  " );
		setIconImage( Images.get( "ICON" ) );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
		setContentPane( new ChessPanel() );
		pack();
		setResizable( false );
		setVisible( true );
		//repaint();
	}
	

}
