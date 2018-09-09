package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.helper.Move
import voidchess.helper.Position
import voidchess.image.ImageType

/**
 * @author stephan
 */
internal class MockFigure(isWhite: Boolean, position: Position) : Figure(isWhite, position, FigureType.MOCK) {

    override fun isReachable(to: Position, game: BasicChessGameInterface) = false
    override fun isSelectable(game: SimpleChessBoardInterface) = true
    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {}
    override fun countReachableMoves(game: BasicChessGameInterface) = 0
}
