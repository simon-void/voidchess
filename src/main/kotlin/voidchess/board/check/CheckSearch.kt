package voidchess.board.check

import voidchess.board.BasicChessGameInterface
import voidchess.board.getFirstFigureInDir
import voidchess.board.move.Direction
import voidchess.board.move.Position
import voidchess.figures.King


object CheckSearch {
    private val doNotCollectPositions: (pos: Position) -> Unit = {}

    fun isCheck(game: BasicChessGameInterface, king: King): Boolean {
        val isWhite = king.isWhite
        val kingPos = king.position

        if (isCheckByBishopOrQueen(game, kingPos, isWhite, doNotCollectPositions)) return true
        if (isCheckByRookOrQueen(game, kingPos, isWhite, doNotCollectPositions)) return true
        if (isCheckByKnight(game, kingPos, isWhite, doNotCollectPositions)) return true
        return if (isCheckByKing(game, kingPos, doNotCollectPositions)) true
        else isCheckByPawn(game, kingPos, isWhite, doNotCollectPositions)
    }

    private inline fun isCheckByKing(game: BasicChessGameInterface, kingPos: Position, informOfAttackerPos: (Position) -> Unit): Boolean {
        Direction.values().forEach {
            kingPos.step(it)?.let { pos ->
                game.getFigureOrNull(pos)?.let { figure ->
                    if (figure.isKing()) {
                        informOfAttackerPos(pos)
                        return true
                    }
                }
            }
        }

        return false
    }

    private inline fun isCheckByPawn(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        val forwardDir = Direction.getForward(isWhite)

        kingPos.step(Direction.getDiagonal(forwardDir, Direction.RIGHT))?.let { pos ->
            game.getFigureOrNull(pos)?.let { figure ->
                if (figure.isPawn() && figure.isWhite != isWhite) {
                    informOfAttackerPos(pos)
                    return true
                }
            }
        }
        kingPos.step(Direction.getDiagonal(forwardDir, Direction.LEFT))?.let { pos ->
            game.getFigureOrNull(pos)?.let { figure ->
                if (figure.isPawn() && figure.isWhite != isWhite) {
                    informOfAttackerPos(pos)
                    return true
                }
            }
        }

        return false
    }

    private inline fun isCheckByKnight(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        kingPos.forEachKnightPos { pos ->
            game.getFigureOrNull(pos)?.let { figure ->
                if (figure.isWhite != isWhite && figure.isKnight()) {
                    informOfAttackerPos(pos)
                    return true
                }
            }
        }

        return false
    }

    private inline fun isCheckByBishopOrQueen(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        Direction.diagonalDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isBishop())) {
                    informOfAttackerPos(figure.position)
                    return true
                }
            }
        }
        return false
    }

    private inline fun isCheckByRookOrQueen(game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {

        if (isDoubleHorizontalCheckAfterPawnPromotion(game, kingPos, isWhite, informOfAttackerPos)) return true
        // now that a straight double attack after pawn promotion is no longer an issue,
        // we can stop looking for a second attacker from straight lines

        Direction.straightDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                    informOfAttackerPos(figure.position)
                    return true
                }
            }
        }
        return false
    }

    private inline fun isDoubleHorizontalCheckAfterPawnPromotion(
            game: BasicChessGameInterface, kingPos: Position, isWhite: Boolean, informOfAttackerPos: (Position) -> Unit): Boolean {
        //only possible if the king stod in the columnshadow of a pawn which transformed in the last move to Rook or queen
        val groundRow = if (isWhite) 0 else 7
        if (kingPos.row != groundRow) return false

        var kingSideAttackerPos: Position? = null

        kingPos.step(Direction.LEFT)?.let { sidePos ->
            game.getFigureOrNull(sidePos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                    kingSideAttackerPos = sidePos
                }
            }
        }
        if (kingSideAttackerPos == null) {
            kingPos.step(Direction.RIGHT)?.let { sidePos ->
                game.getFigureOrNull(sidePos)?.let { figure ->
                    if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                        kingSideAttackerPos = sidePos
                    }
                }
            }
        }

        if (kingSideAttackerPos == null) return false

        val dirOfPossibleSecondStraightAttacker = if (isWhite) Direction.UP else Direction.DOWN

        game.getFirstFigureInDir(dirOfPossibleSecondStraightAttacker, kingPos)?.let { figure ->
            if (figure.isWhite != isWhite && (figure.isQueen() || figure.isRook())) {
                informOfAttackerPos(kingSideAttackerPos!!)
                informOfAttackerPos(figure.position)
                return true
            }
        }

        return false
    }
}
