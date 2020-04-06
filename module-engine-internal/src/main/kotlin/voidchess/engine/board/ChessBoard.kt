package voidchess.engine.board

import voidchess.common.board.move.Move
import voidchess.engine.board.check.AttackLines
import voidchess.engine.figures.Figure
import voidchess.common.board.move.Position


internal interface ChessBoard : BasicChessBoard {

    fun init()
    fun init(chess960: Int)
    fun init(des: String)
    fun setFigure(pos: Position, figure: Figure)
    fun clearFigure(pos: Position): Figure
    fun clearPos(pos: Position)
    /**
     * @return true if a figure got hit
     */
    fun move(move: Move, supervisor: ChessGameSupervisor): Boolean
    fun undo(): Boolean
    fun movesPlayed(): List<Move>
    // moves figure, returns the figure that was taken
    // "normal" moves only, no special cases (castling, enpassant, pawn promotion)
    fun move(figure: Figure, to: Position): Figure?
    fun undoMove(figure: Figure, from: Position, figureTaken: Figure?)
    fun isCheck(isWhite: Boolean): Boolean
    fun getCachedAttackLines(isWhite: Boolean): AttackLines
    fun historyToString(numberOfHalfMoves: Int?=null): String
}

//internal inline fun ChessBoard.simulateSimplifiedMove(figure: Figure, warpTo: Position, query: (BasicChessBoard) -> Boolean): Boolean {
//    val fromPos = figure.position
//    // warp figure to new position
////    val figureTaken = move(figure, warpTo)
//    clearPos(figure.position)
//    val figureTaken = getFigureOrNull(warpTo)
//    setFigure(warpTo, figure)
//    figure.figureMoved( Move[figure.position, warpTo])
//    // execute the query with figure on the new position
//    val result = query(this)
//    // move the figure back to it's original position
////    undoMove(figure, fromPos, figureTaken)
//    if(figureTaken==null) {
//        clearPos(figure.position)
//    }else{
//        setFigure(figure.position, figureTaken)
//    }
//    setFigure(fromPos, figure)
//    figure.undoMove(fromPos)
//    // return the result of the query
//    return result
//}
//
//// TODO as soon as inline function are allowed to have internal methods move these two methods into ChessBoard.simulateSimplifiedMove
//internal fun ChessBoard.move(figure: Figure, to: Position): Figure? {
//    val move = Move[figure.position, to]
//    clearPos(figure.position)
//    val figureTaken = getFigureOrNull(to)
//    setFigure(to, figure)
//    figure.figureMoved(move)
//    return figureTaken
//}
//internal fun ChessBoard.undoMove(figure: Figure, from: Position, figureTaken: Figure?) {
//    if(figureTaken==null) {
//        clearPos(figure.position)
//    }else{
//        setFigure(figure.position, figureTaken)
//    }
//    setFigure(from, figure)
//    figure.undoMove(from)
//}
