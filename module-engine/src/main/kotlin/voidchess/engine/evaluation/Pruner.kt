package voidchess.engine.evaluation

import voidchess.engine.evaluation.SearchTreePruner.Companion.MAX_SEARCH_DEPTH

internal interface SearchTreePruner {

    fun continueMinDynamicEvaluationBy(depth: Int, thisMove_isChess: Boolean, thisMove_hasHitFigure: Boolean,
                                       lastMove_isChess: Boolean, lastMove_hasHitFigure: Boolean): ContinueEvalBy

    fun continueMaxDynamicEvaluationBy(depth: Int, thisMove_isChess: Boolean, thisMove_hasHitFigure: Boolean,
                                       lastMove_isChess: Boolean, lastMove_hasHitFigure: Boolean): ContinueEvalBy

    companion object {
        // everything higher seems to be unachievable at the moment
        const val MAX_SEARCH_DEPTH = 8
    }
}

internal enum class ContinueEvalBy {
    StaticEval, AllMoves, IrreversibleMoves, TakingMoves
}

internal class PrunerWithIrreversibleMoves(private val allMoveRadius: Int = 1, private val irreversibleMoveRadius: Int = 2, private val takingMoveRadius: Int = 4, private val lookForCheckMateRadius: Int = 3) : SearchTreePruner {

    init {
        require (allMoveRadius >= 1) {"allMoveRadius has to be at least one but is $allMoveRadius"}
        require (allMoveRadius <= irreversibleMoveRadius) {"irreversibleMoveRadius ($irreversibleMoveRadius) has to be equall or bigger allMoveRadius ($allMoveRadius)"}
        require (takingMoveRadius in irreversibleMoveRadius..MAX_SEARCH_DEPTH) {"takingMoveRadius has to be in [$irreversibleMoveRadius, $MAX_SEARCH_DEPTH] but is $takingMoveRadius"}
        require (lookForCheckMateRadius in irreversibleMoveRadius..MAX_SEARCH_DEPTH) {"lookForCheckMateRadius has to be in [$irreversibleMoveRadius, $MAX_SEARCH_DEPTH] but is $lookForCheckMateRadius"}
    }

    override fun continueMinDynamicEvaluationBy(depth: Int,
                                                thisMove_isChess: Boolean,
                                                thisMove_hasHitFigure: Boolean,
                                                lastMove_isChess: Boolean,
                                                lastMove_hasHitFigure: Boolean): ContinueEvalBy {
        return when {
            depth<allMoveRadius -> ContinueEvalBy.AllMoves
            (thisMove_isChess || lastMove_isChess) -> ContinueEvalBy.AllMoves
            depth<irreversibleMoveRadius -> ContinueEvalBy.IrreversibleMoves
            thisMove_hasHitFigure -> ContinueEvalBy.TakingMoves
            else -> ContinueEvalBy.StaticEval
        }
    }

    override fun continueMaxDynamicEvaluationBy(depth: Int,
                                                thisMove_isChess: Boolean,
                                                thisMove_hasHitFigure: Boolean,
                                                lastMove_isChess: Boolean,
                                                lastMove_hasHitFigure: Boolean): ContinueEvalBy {
        return when {
            depth<allMoveRadius -> ContinueEvalBy.AllMoves
            (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> ContinueEvalBy.AllMoves
            depth<irreversibleMoveRadius -> ContinueEvalBy.AllMoves  // intentionally not ContinueEvalBy.IrreversibleMoves (but open to rethink)
            thisMove_hasHitFigure && depth<takingMoveRadius -> ContinueEvalBy.TakingMoves
            else -> ContinueEvalBy.StaticEval
        }
    }
}

internal class AllMovesOrNonePruner(private val allMoveRadius: Int = 1, private val takingMoveRadius: Int = 4, private val lookForCheckMateRadius: Int = 3) : SearchTreePruner {

    init {
        require (allMoveRadius >= 1) {"allMoveRadius has to be at least one but is $allMoveRadius"}
        require (takingMoveRadius in allMoveRadius..MAX_SEARCH_DEPTH) {"takingMoveRadius has to be in [$allMoveRadius, $MAX_SEARCH_DEPTH] but is $takingMoveRadius"}
        require (lookForCheckMateRadius in allMoveRadius..MAX_SEARCH_DEPTH) {"lookForCheckMateRadius has to be in [$allMoveRadius, $MAX_SEARCH_DEPTH] but is $lookForCheckMateRadius"}
    }

    override fun continueMinDynamicEvaluationBy(depth: Int,
                                                thisMove_isChess: Boolean,
                                                thisMove_hasHitFigure: Boolean,
                                                lastMove_isChess: Boolean,
                                                lastMove_hasHitFigure: Boolean): ContinueEvalBy {
        return if(thisMove_hasHitFigure || thisMove_isChess || lastMove_isChess || depth < allMoveRadius) {
            ContinueEvalBy.AllMoves
        } else {
            ContinueEvalBy.StaticEval
        }
    }

    override fun continueMaxDynamicEvaluationBy(depth: Int,
                                                thisMove_isChess: Boolean,
                                                thisMove_hasHitFigure: Boolean,
                                                lastMove_isChess: Boolean,
                                                lastMove_hasHitFigure: Boolean): ContinueEvalBy {
        return when {
            depth<allMoveRadius -> ContinueEvalBy.AllMoves
            thisMove_hasHitFigure && depth<takingMoveRadius -> ContinueEvalBy.AllMoves
            (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> ContinueEvalBy.AllMoves
            else -> ContinueEvalBy.StaticEval
        }
    }
}