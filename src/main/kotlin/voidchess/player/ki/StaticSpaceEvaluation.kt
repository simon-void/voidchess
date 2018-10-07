package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.figures.Figure
import voidchess.helper.Direction
import voidchess.helper.Position
import voidchess.player.ki.evaluation.Evaluated
import voidchess.player.ki.evaluation.Ongoing

import java.util.HashSet
import java.util.LinkedList


class StaticSpaceEvaluation : StaticEvaluationInterface {

    override fun getPrimaryEvaluation(game: ChessGameInterface, forWhite: Boolean): Ongoing {
        val basicValue = 200
        var whiteKing: Figure? = null
        var blackKing: Figure? = null

        var colorOfWinnerIsWhite = true
        val figuresPos = LinkedList<Position>()

        for (index in 0..63) {
            val pos = Position.byIndex(index)
            val content = game.getContent(pos)
            if (!content.isFreeArea) {
                val figure = content.figure
                if (figure.isKing()) {
                    if (figure.isWhite)
                        whiteKing = figure
                    else
                        blackKing = figure
                } else {
                    colorOfWinnerIsWhite = figure.isWhite
                    figuresPos.add(pos)
                }
            }
        }
        // verify that white and black king where found
        whiteKing!!
        blackKing!!

        var value: Double
        val king = if (colorOfWinnerIsWhite) blackKing else whiteKing
        if (colorOfWinnerIsWhite) {
            figuresPos.add(whiteKing.position)
        } else {
            figuresPos.add(blackKing.position)
        }

        value = countRestSpace(game, king, figuresPos).toDouble()
        value += 3 * borderMalus(king.position)
        value += (5 * getKingDistance(whiteKing.position, blackKing.position)).toDouble()
        value -= basicValue.toDouble()

        if (colorOfWinnerIsWhite == forWhite) value = -value

        //TODO is there is better secondary evaluation that a static 0?
        return Ongoing(value / 10.0)
    }

    override fun addSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean, evaluated: Evaluated) {
        //do nothing (keep the secondary value = 0)
        //TODO is there a good secondary matrix of success for space?
    }

    fun getKingDistance(pos1: Position, pos2: Position): Int {
        //return	Math.max( Math.abs(pos1.row-pos2.row),Math.abs(pos1.column-pos2.column) );
        return Math.abs(pos1.row - pos2.row) + Math.abs(pos1.column - pos2.column)
    }

    private fun borderMalus(kingPos: Position): Double {
        return -Math.max(Math.abs(kingPos.row - 3.5), Math.abs(kingPos.column - 3.5))
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
            for (index in 0..63) {
                val content = game.getContent(Position.byIndex(index))
                if (!content.isFreeArea) {
                    val figure = content.figure
                    if (figure.isWhite) {
                        whiteFigures++
                    } else {
                        blackFigures++
                    }
                    if (figure.isPawn() || whiteFigures > 1 && blackFigures > 1) return false
                }
            }
            return true
        }
    }
}
