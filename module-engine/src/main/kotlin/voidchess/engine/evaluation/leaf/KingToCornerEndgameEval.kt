package voidchess.engine.evaluation.leaf

import voidchess.common.board.move.Position
import voidchess.common.engine.Ongoing
import kotlin.math.abs

internal object KingToCornerEndgameEval : StaticEval() {
    override fun getNumericEvaluation(
        game: FixedBoard,
        forWhite: Boolean,
        isWhiteTurn: Boolean // not used in this case
    ): Ongoing {
        val materialEvalForWhite = evaluateFigures(game, Colour(true), isWhiteTurn)
        val whiteIsWinning = materialEvalForWhite > 0

        val winningKingPos: Position
        val loosingKingPos: Position
        if (whiteIsWinning) {
            winningKingPos = game.whiteKingPos
            loosingKingPos = game.blackKingPos
        } else {
            winningKingPos = game.blackKingPos
            loosingKingPos = game.whiteKingPos
        }

        val evalForWinningSide = abs(materialEvalForWhite) +
                manhattanDistanceToCenter(loosingKingPos) - (manhattanDistance(loosingKingPos, winningKingPos) / 3.0)

        return if (whiteIsWinning == forWhite) {
            Ongoing(evalForWinningSide)
        } else {
            Ongoing(-evalForWinningSide)
        }
    }

    /**
     * @returns distance to center point (3.5, 3.5) -> 1 .. 7
      */
    private fun manhattanDistanceToCenter(pos: Position): Double =
        abs(3.5-pos.column) + abs(3.5-pos.row)

    /**
     * @returns manhattan distance between both (king) positions 2 .. 14
     */
    private fun manhattanDistance(pos1: Position, pos2: Position): Double =
        (abs(pos1.row - pos2.row) + abs(pos1.column - pos2.column)).toDouble()
}