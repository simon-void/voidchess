/*
 * Created on 08.11.2006
 */

package player.ki;

public class FullMovePruner
extends SimplePruner
{
	public FullMovePruner()
	{
		this( 1,2,2 );
	}
	
	public FullMovePruner( int no_hit_radius,int figure_hit_radius,int chess_radius )
	{
		super( no_hit_radius,figure_hit_radius,chess_radius );
	}
	
	public boolean stopMinDynamicEvaluation( int depth,
	                                         boolean thisMove_isChess,
	                                         boolean thisMove_hasHitFigure,
	                                         boolean lastMove_isChess,
	                                         boolean lastMove_hasHitFigure )
	{		
		return false;
	}
}
