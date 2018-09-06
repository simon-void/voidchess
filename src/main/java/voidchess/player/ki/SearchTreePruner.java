/*
 * Created on 05.11.2006
 */


package voidchess.player.ki;

public interface SearchTreePruner {

    boolean stopMinDynamicEvaluation(int depth, boolean thisMove_isChess, boolean thisMove_hasHitFigure,
                                     boolean lastMove_isChess, boolean lastMove_hasHitFigure);

    boolean stopMaxDynamicEvaluation(int depth, boolean thisMove_isChess, boolean thisMove_hasHitFigure,
                                     boolean lastMove_isChess, boolean lastMove_hasHitFigure);

}