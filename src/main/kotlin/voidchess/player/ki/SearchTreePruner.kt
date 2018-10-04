package voidchess.player.ki

interface SearchTreePruner {

    fun stopMinDynamicEvaluation(depth: Int, thisMove_isChess: Boolean, thisMove_hasHitFigure: Boolean,
                                 lastMove_isChess: Boolean, lastMove_hasHitFigure: Boolean): Boolean

    fun stopMaxDynamicEvaluation(depth: Int, thisMove_isChess: Boolean, thisMove_hasHitFigure: Boolean,
                                 lastMove_isChess: Boolean, lastMove_hasHitFigure: Boolean): Boolean

}