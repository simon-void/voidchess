package voidchess.engine.evaluation.leaf

import voidchess.common.board.move.Position
import voidchess.common.player.ki.evaluation.Ongoing
import voidchess.engine.board.EngineChessGame
import kotlin.math.abs
import kotlin.math.max

internal object KingToEdgeEndgameEval : StaticEval() {
    override fun getNumericEvaluation(game: EngineChessGame, forWhite: Boolean): Ongoing {
        val materialEvalForWhite = evaluateFigures(game, true)
        val whiteIsWinning = materialEvalForWhite > 0

        val winningKingPos: Position
        val loosingKingPos: Position
        if (whiteIsWinning) {
            winningKingPos = game.whiteKing.position
            loosingKingPos = game.blackKing.position
        } else {
            winningKingPos = game.blackKing.position
            loosingKingPos = game.whiteKing.position
        }

        val evalForWinningSide = abs(materialEvalForWhite) +
                (maxDistanceToCenter(loosingKingPos) * 2) - (manhattanDistance(loosingKingPos, winningKingPos) / 7.0)

        return if (whiteIsWinning == forWhite) {
            Ongoing(evalForWinningSide)
        } else {
            Ongoing(-evalForWinningSide)
        }
    }

    /**
     * @returns distance to center point (3.5, 3.5) -> 0.5 .. 3.5
      */
    private fun maxDistanceToCenter(pos: Position): Double =
        max(abs(3.5-pos.column), abs(3.5-pos.row))

    /**
     * @returns manhattan distance between both (king) positions 2 .. 14
     */
    private fun manhattanDistance(pos1: Position, pos2: Position): Double =
        (abs(pos1.row - pos2.row) + abs(pos1.column - pos2.column)).toDouble()
}