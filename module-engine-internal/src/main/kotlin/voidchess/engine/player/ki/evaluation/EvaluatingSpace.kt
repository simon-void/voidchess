package voidchess.engine.player.ki.evaluation

import voidchess.engine.board.ChessGameInterface
import voidchess.engine.board.forAllFigures
import voidchess.engine.figures.Figure
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position

import java.util.HashSet
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.max


internal class EvaluatingSpace : EvaluatingStatically {

    override fun getPreliminaryEvaluation(game: ChessGameInterface, forWhite: Boolean): Double {
        val basicValue = 200
        val whiteKing = game.whiteKing
        val blackKing = game.blackKing

        var colorOfWinnerIsWhite = true
        val figuresPos = LinkedList<Position>()

        game.forAllFigures { figure ->
            if (!figure.isKing()) {
                colorOfWinnerIsWhite = figure.isWhite
                figuresPos.add(figure.position)
            }
        }

        val king = if (colorOfWinnerIsWhite) blackKing else whiteKing
        if (colorOfWinnerIsWhite) {
            figuresPos.add(whiteKing.position)
        } else {
            figuresPos.add(blackKing.position)
        }

        var value = countRestSpace(game, king, figuresPos).toDouble()
        value += 3 * borderMalus(king.position)
        value += (5 * whiteKing.position.distanceTo(blackKing.position)).toDouble()
        value -= basicValue.toDouble()

        if (colorOfWinnerIsWhite == forWhite) value = -value

        return value / 10.0
    }

    //TODO is there a good secondary matrix of success for space?
    override fun getSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean) = 0.0

    override fun getCheckmateMaterialEvaluation(game: ChessGameInterface, forWhite: Boolean) = 0.0

    private fun borderMalus(kingPos: Position): Double {
        return -max(abs(kingPos.row - 3.5), abs(kingPos.column - 3.5))
    }

    fun countRestSpace(game: ChessGameInterface,
                       king: Figure,
                       otherFiguresPos: List<Position>): Int {
        val figuresHaveWhiteColor = !king.isWhite
        val foundPositions = HashSet<Position>(64)
        var searchPositions = HashSet<Position>(2)
        foundPositions.add(king.position)
        searchPositions.add(king.position)
        while (searchPositions.isNotEmpty()) {
            val newSearchPositions = HashSet<Position>(10, 1f)
            for (searchPos in searchPositions) {
                val newIter = getPossibleKingPositions(searchPos)
                while (newIter.hasNext()) {
                    val foundPos = newIter.next()
                    if (!foundPositions.contains(foundPos) && isNotReachableByFiguresInList(
                                    foundPos,
                                    otherFiguresPos,
                                    game,
                                    figuresHaveWhiteColor)) {
                        foundPositions.add(foundPos)
                        newSearchPositions.add(foundPos)
                    }
                }
            }
            searchPositions = newSearchPositions
        }

        return foundPositions.size
    }

    private fun isNotReachableByFiguresInList(to: Position,
                                              other: List<Position>,
                                              game: ChessGameInterface,
                                              figuresHaveWhiteColor: Boolean): Boolean {
        for (anOther in other) {
            if (anOther.equalsPosition(to)) return false
            if (game.isMovable(anOther, to, figuresHaveWhiteColor)) return false
        }
        return true
    }

    private fun getPossibleKingPositions(pos: Position): Iterator<Position> {
        val positions = LinkedList<Position>()
        for(direction in Direction.values()) {
            pos.step(direction)?.let {newPos ->
                positions.add(newPos)
            }
        }
        return positions.iterator()
    }

    companion object {

        fun shouldUseStaticSpaceEvaluation(game: ChessGameInterface): Boolean {
            var whiteFigures = 0
            var blackFigures = 0
            game.forAllFigures { figure ->
                if (figure.isWhite) {
                    whiteFigures++
                } else {
                    blackFigures++
                }
                if (figure.isPawn() || whiteFigures > 1 && blackFigures > 1) return false
            }
            return true
        }
    }
}
