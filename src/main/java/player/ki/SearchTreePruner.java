/*
 * Created on 05.11.2006
 */


package player.ki;

public interface SearchTreePruner
{

	public boolean stopMinDynamicEvaluation( int depth,boolean thisMove_isChess,boolean thisMove_hasHitFigure,
												boolean lastMove_isChess,boolean lastMove_hasHitFigure );

	public boolean stopMaxDynamicEvaluation( int depth,boolean thisMove_isChess,boolean thisMove_hasHitFigure,
												boolean lastMove_isChess,boolean lastMove_hasHitFigure );

}