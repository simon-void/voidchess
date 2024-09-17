package voidchess.engine.evaluation

import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.isPawnMove
import voidchess.engine.evaluation.SearchTreePruner.Companion.MAX_SEARCH_DEPTH

internal interface SearchTreePruner {

    val allMoveRadius: Int

    fun continueMinDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean


    fun continueMaxDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean

    companion object {
        // everything higher seems to be unachievable at the moment
        const val MAX_SEARCH_DEPTH = 8
    }
}

internal class PrunerWithPawnMoves(override val allMoveRadius: Int = 2, private val pawnMoveRadius: Int = 3, private val takingMoveRadius: Int = 4, private val lookForCheckMateRadius: Int = 3) : SearchTreePruner {

    init {
        require (allMoveRadius >= 2) {"allMoveRadius has to be at least 2 but is $allMoveRadius"}
        require (allMoveRadius <= pawnMoveRadius) {"pawnMoveRadius ($pawnMoveRadius) has to be equall or bigger allMoveRadius ($allMoveRadius)"}
        require (takingMoveRadius in pawnMoveRadius..MAX_SEARCH_DEPTH) {"takingMoveRadius has to be in [$pawnMoveRadius, $MAX_SEARCH_DEPTH] but is $takingMoveRadius"}
        require (lookForCheckMateRadius in pawnMoveRadius..MAX_SEARCH_DEPTH) {"lookForCheckMateRadius has to be in [$pawnMoveRadius, $MAX_SEARCH_DEPTH] but is $lookForCheckMateRadius"}
    }

    override fun continueMinDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth < allMoveRadius -> true
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        thisMove_extendedMove.isPawnMove() && depth < pawnMoveRadius -> true
        else -> false
    }

    override fun continueMaxDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth < allMoveRadius -> true
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        thisMove_extendedMove.isPawnMove() && depth < pawnMoveRadius -> true
        else -> false
    }
}

internal class DefaultPruner(override val allMoveRadius: Int = 2, private val takingMoveRadius: Int = 4, private val lookForCheckMateRadius: Int = 3) : SearchTreePruner {

    init {
        require (allMoveRadius >= 2) {"allMoveRadius has to be at least 2 but is $allMoveRadius"}
        require (takingMoveRadius in allMoveRadius..MAX_SEARCH_DEPTH) {"takingMoveRadius has to be in [$allMoveRadius, $MAX_SEARCH_DEPTH] but is $takingMoveRadius"}
        require (lookForCheckMateRadius in allMoveRadius..MAX_SEARCH_DEPTH) {"lookForCheckMateRadius has to be in [$allMoveRadius, $MAX_SEARCH_DEPTH] but is $lookForCheckMateRadius"}
    }

    override fun continueMinDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth < allMoveRadius -> true
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        else -> false
    }

    override fun continueMaxDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth < allMoveRadius -> true
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        else -> false
    }
}

internal class SingleFullMovePruner(private val takingMoveRadius: Int = 4, private val lookForCheckMateRadius: Int = 3) : SearchTreePruner {

    init {
        require (takingMoveRadius in 1..MAX_SEARCH_DEPTH) {"takingMoveRadius has to be in [$1, $MAX_SEARCH_DEPTH] but is $takingMoveRadius"}
        require (lookForCheckMateRadius in 1..MAX_SEARCH_DEPTH) {"lookForCheckMateRadius has to be in [$1, $MAX_SEARCH_DEPTH] but is $lookForCheckMateRadius"}
    }

    override val allMoveRadius = 1

    override fun continueMinDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth==1 &&  (thisMove_extendedMove.hasHitFigure || lastMove_extendedMove.hasHitFigure) -> true // to guard against sacrificing a figure to delay castleing
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        else -> false
    }

    override fun continueMaxDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth==1 &&  (thisMove_extendedMove.hasHitFigure || lastMove_extendedMove.hasHitFigure) -> true // to guard against sacrificing a figure to delay castleing
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        else -> false
    }
}

internal class SingleFullMovePrunerWithPawnMoves(private val pawnMoveRadius: Int = 2, private val takingMoveRadius: Int = 4, private val lookForCheckMateRadius: Int = 3) : SearchTreePruner {

    init {
        require (1 <= pawnMoveRadius) {"pawnMoveRadius ($pawnMoveRadius) has to be equall or bigger 1"}
        require (takingMoveRadius in pawnMoveRadius..MAX_SEARCH_DEPTH) {"takingMoveRadius has to be in [$pawnMoveRadius, $MAX_SEARCH_DEPTH] but is $takingMoveRadius"}
        require (lookForCheckMateRadius in pawnMoveRadius..MAX_SEARCH_DEPTH) {"lookForCheckMateRadius has to be in [$pawnMoveRadius, $MAX_SEARCH_DEPTH] but is $lookForCheckMateRadius"}
    }

    override val allMoveRadius = 1

    override fun continueMinDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth==1 &&  (thisMove_extendedMove.hasHitFigure || lastMove_extendedMove.hasHitFigure) -> true // to guard against sacrificing a figure to delay castleing
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        thisMove_extendedMove.isPawnMove() && depth < pawnMoveRadius -> true
        else -> false
    }

    override fun continueMaxDynamicEvaluationBy(
            depth: Int,
            thisMove_isChess: Boolean,
            lastMove_isChess: Boolean,
            thisMove_extendedMove: ExtendedMove,
            lastMove_extendedMove: ExtendedMove
    ): Boolean = when {
        depth==1 &&  (thisMove_extendedMove.hasHitFigure || lastMove_extendedMove.hasHitFigure) -> true // to guard against sacrificing a figure to delay castleing
        thisMove_extendedMove.hasHitFigure && depth < takingMoveRadius -> true
        (thisMove_isChess || lastMove_isChess) && depth < lookForCheckMateRadius -> true
        thisMove_extendedMove.isPawnMove() && depth < pawnMoveRadius -> true
        else -> false
    }
}