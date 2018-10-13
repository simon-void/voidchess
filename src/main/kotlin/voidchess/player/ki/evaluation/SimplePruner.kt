package voidchess.player.ki.evaluation


open class SimplePruner(private val NO_HIT_RADIUS: Int = 1, private val FIGURE_HIT_RADIUS: Int = 2, private val CHESS_RADIUS: Int = 2) : SearchTreePruner {

    init {
        if (NO_HIT_RADIUS < 1) throw IllegalArgumentException("no_hit_radius has to be at least one")
        if (NO_HIT_RADIUS > MAX_SEARCH_DEPTH) throw IllegalArgumentException("no_hit_radius can't be bigger than MAX_SEARCH_DEPTH")
        if (FIGURE_HIT_RADIUS < NO_HIT_RADIUS)
            throw IllegalArgumentException("figure_hit_radius cann't be smaller than no_hit_radius")
        if (FIGURE_HIT_RADIUS > MAX_SEARCH_DEPTH) throw IllegalArgumentException("figure_hit_radius can't be bigger than MAX_SEARCH_DEPTH")
        if (CHESS_RADIUS < NO_HIT_RADIUS)
            throw IllegalArgumentException("chess_radius cann't be smaller than no_hit_radius")
        if (CHESS_RADIUS > MAX_SEARCH_DEPTH) throw IllegalArgumentException("chess_radius can't be bigger than MAX_SEARCH_DEPTH")
    }

    override fun stopMinDynamicEvaluation(depth: Int,
                                          thisMove_isChess: Boolean,
                                          thisMove_hasHitFigure: Boolean,
                                          lastMove_isChess: Boolean,
                                          lastMove_hasHitFigure: Boolean): Boolean {
        //return false;
        return !(depth < NO_HIT_RADIUS || thisMove_hasHitFigure || thisMove_isChess || lastMove_isChess)
    }

    override fun stopMaxDynamicEvaluation(depth: Int,
                                          thisMove_isChess: Boolean,
                                          thisMove_hasHitFigure: Boolean,
                                          lastMove_isChess: Boolean,
                                          lastMove_hasHitFigure: Boolean): Boolean {
        if (depth < NO_HIT_RADIUS) return false                                    // no figure was taken
        if (depth < FIGURE_HIT_RADIUS && thisMove_hasHitFigure) return false       // a figure was taken

        return !(depth < CHESS_RADIUS && (thisMove_isChess || lastMove_isChess))   // check now or a half move ago
    }

    companion object {
        const val MAX_SEARCH_DEPTH = 6
    }
}
