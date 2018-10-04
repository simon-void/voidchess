/*
 * Created on 08.11.2006
 */

package voidchess.player.ki

class FullMovePruner constructor(no_hit_radius: Int, figure_hit_radius: Int, chess_radius: Int): SimplePruner(no_hit_radius, figure_hit_radius, chess_radius) {

    override fun stopMinDynamicEvaluation(depth: Int,
                                          thisMove_isChess: Boolean,
                                          thisMove_hasHitFigure: Boolean,
                                          lastMove_isChess: Boolean,
                                          lastMove_hasHitFigure: Boolean): Boolean {
        return false
    }
}
